package ir.daneshrefah.scm.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.event.Event;
import ir.daneshrefah.scm.common.model.event.InboundEvent;
import ir.daneshrefah.scm.common.model.event.OutboundEvent;
import ir.daneshrefah.scm.common.model.event.ServiceEvent;
import ir.daneshrefah.scm.log.config.ApplicationConfig;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.entity.TransactionLog;
import ir.daneshrefah.scm.log.repository.TransactionLogRepository;
import ir.daneshrefah.scm.utils.date.DateUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;

@Service
@AllArgsConstructor
public class TransactionLogService {

    private final TransactionLogRepository transactionLogRepository;

    @Transactional
    public void save(String msg) {
        TransactionLog transactionLog = new TransactionLog();
        try {
            ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();
            LogMessage logMessage = convertToLogMessage(msg);
            convertToTransactionLog(transactionLog, logMessage);
            transactionLog.setPayload(objectMapper.writeValueAsString(logMessage));
        } catch (Exception e) {
            String payload = formattedException(e, msg);
            transactionLog.setPayload(payload);
        }
        transactionLogRepository.save(transactionLog);
    }

    private static LogMessage convertToLogMessage(String msg) throws JsonProcessingException {
        ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();
        return objectMapper.readValue(msg, LogMessage.class);
    }

    private void convertToTransactionLog(TransactionLog transactionLog, LogMessage logMessage) {
        Event event = logMessage.getPayload();
        transactionLog.setTerminalCode(event.getTerminalCode());
        transactionLog.setChannelCode(event.getChannelCode());
        transactionLog.setClientId(event.getClientId() != null ? event.getClientId() : event.getTerminalCode());//TODO:This is null. Check it.
        transactionLog.setCorrelationId(event.getCorrelationId());
        transactionLog.setClientCorrelationId(event.getClientCorrelationId());//TODO:This is null. Check it.
        transactionLog.setClientFlowId(event.getClientFlowId()); //TODO:This is null. Check it.
        transactionLog.setServiceCode(event.getServiceCode());
        transactionLog.setNickname(event.getNickname());
        transactionLog.setUsername(event.getUsername());
        transactionLog.setDelegatorNickname(event.getDelegatorNickname());//TODO:This is null. Check it.
        transactionLog.setDelegatorUsername(event.getDelegatorUsername());//TODO:This is null. Check it.
        transactionLog.setMessageId(event.getMessageId());
        transactionLog.setHostAddress(event.getHostAddress());//TODO:This is null. Check it.
        transactionLog.setEventType(event.getEventType() != null ? event.getEventType().toString() : null);
        switch (event.getEventType()) {
            case INBOUND -> populateInboundLog(transactionLog, event);
            case OUTBOUND -> populateOutboundLog(transactionLog, event);
            case SERVICE_CALL -> populateServiceCallLog(transactionLog, event);
        }
        paymentTransferLog(event);
    }

    private static void paymentTransferLog(Event event) {
        if (event.getServiceCode() != null && event.getServiceCode().equalsIgnoreCase("")) {//TODO service code,complete this code snippet
            //  transactionLog.setAmount();//TODO from input
            //transactionLog.setCardNumber();//TODO from input
        }
    }

    private void populateInboundLog(TransactionLog transactionLog, Event event) {
        if (event instanceof InboundEvent inboundEvent) {
            transactionLog.setDurationMills(inboundEvent.getDurationMillis());
            transactionLog.setMessageStatus(inboundEvent.getMessageStatus() != null ? inboundEvent.getMessageStatus().name() : null);
            Instant startTime = inboundEvent.getStartTime();
            Instant endTime = inboundEvent.getEndTime();
            setDate(transactionLog, startTime, endTime);
        }
    }

    private void populateServiceCallLog(TransactionLog transactionLog, Event event) {
        if (event instanceof ServiceEvent serviceEvent) {
            transactionLog.setMessageStatus(serviceEvent.getStatus() != null ? serviceEvent.getStatus().name() : null);
            transactionLog.setExceptionClassName(serviceEvent.getExceptionClassName());// TODO: The wrong exception class is being set because all exceptions are currently being set to the generic Exception type.
            transactionLog.setDurationMills(serviceEvent.getDurationMillis());
            Instant startTime = serviceEvent.getStartTime();
            Instant endTime = serviceEvent.getEndTime();
            setDate(transactionLog, startTime, endTime);
        }
    }

    private static void setDate(TransactionLog transactionLog, Instant startTime, Instant endTime) {
        Date startDate = startTime != null ? DateUtils.DateConverter.convertToDate(startTime) : null;
        transactionLog.setStartTime(startDate);
        Date endDate = endTime != null ? DateUtils.DateConverter.convertToDate(endTime) : null;
        transactionLog.setEndTime(endDate);
    }

    private void populateOutboundLog(TransactionLog transactionLog, Event event) {
        if (event instanceof OutboundEvent outboundEvent) {
            transactionLog.setProviderCode(outboundEvent.getProviderCode());
            transactionLog.setProviderResponseCode(outboundEvent.getProviderResponseCode());//TODO:This is null. Check it.
            transactionLog.setDurationMills(outboundEvent.getDurationMillis());
            transactionLog.setExceptionClassName(outboundEvent.getExceptionClassName());// TODO: The wrong exception class is being set because all exceptions are currently being set to the generic Exception type.
            Instant startTime = outboundEvent.getStartTime();
            Instant endTime = outboundEvent.getEndTime();
            setDate(transactionLog, startTime, endTime);
        }
    }

    public static String formattedException(Exception e, String msg) {
        try {
            ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();

            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("exceptionClassName", e.getClass().getName());
            payloadNode.put("exceptionMessage", e.getMessage());
            ArrayNode stackTraceArray = objectMapper.createArrayNode();
            for (StackTraceElement element : e.getStackTrace()) {
                ObjectNode stackTraceElementNode = objectMapper.createObjectNode();
                stackTraceElementNode.put("className", element.getClassName());
                stackTraceElementNode.put("methodName", element.getMethodName());
                stackTraceElementNode.put("fileName", element.getFileName());
                stackTraceElementNode.put("lineNumber", element.getLineNumber());
                stackTraceArray.add(stackTraceElementNode);
            }
            payloadNode.set("stackTrace", stackTraceArray);
            payloadNode.put("payload", msg);
            return objectMapper.writeValueAsString(payloadNode);
        } catch (Exception ex) {
            ex.printStackTrace();
            return msg;
        }
    }
}
