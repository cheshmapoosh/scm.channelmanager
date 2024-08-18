package ir.daneshrefah.scm.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.audit.AuditEvent;
import ir.daneshrefah.scm.common.model.event.Event;
import ir.daneshrefah.scm.common.model.event.InboundEvent;
import ir.daneshrefah.scm.common.model.event.OutboundEvent;
import ir.daneshrefah.scm.common.model.event.ServiceEvent;
import ir.daneshrefah.scm.common.model.message.HttpMessageInput;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.log.config.ApplicationConfig;
import ir.daneshrefah.scm.log.entity.AbstractLogEntity;
import ir.daneshrefah.scm.log.entity.AuditLogEntity;
import ir.daneshrefah.scm.log.entity.TransactionLogEntity;
import ir.daneshrefah.scm.log.model.LogMessage;
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

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    public void save(String msg) {
        AbstractLogEntity abstractLogEntity = new TransactionLogEntity();
        try {
            ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();
            LogMessage logMessage = convertToLogMessage(msg);
            abstractLogEntity = createAbstractLogEntity(logMessage);
            abstractLogEntity.setPayload(objectMapper.writeValueAsString(logMessage));
        } catch (Exception e) {
            String payload = createExceptionLog(e, msg);
            abstractLogEntity.setPayload(payload);
        }
        transactionLogRepository.save(abstractLogEntity);
    }

    private LogMessage convertToLogMessage(String msg) throws JsonProcessingException {
        ObjectMapper objectMapper = ApplicationConfig.getObjectMapperInstance();
        return objectMapper.readValue(msg, LogMessage.class);
    }

    private AbstractLogEntity createAbstractLogEntity(LogMessage logMessage) {
        Event event = logMessage.getPayload();
        if (event instanceof AuditEvent auditEvent) {
            return createAuditLog(auditEvent);
        }
        return createTransactionLog(event);
    }

    private AbstractLogEntity createAuditLog(AuditEvent auditEvent) {
        AuditLogEntity auditLog = new AuditLogEntity();
        auditLog.setModifyBy(auditEvent.getModifyBy());
        auditLog.setClassType(auditEvent.getClassTypeStr());
        auditLog.setTimestamp(auditEvent.getTimestamp());
        auditLog.setRevisionType(auditEvent.getRevisionType() != null ? auditEvent.getRevisionType().toString() : null);
        return auditLog;
    }

    private AbstractLogEntity createTransactionLog(Event event) {
        TransactionLogEntity transactionLog = new TransactionLogEntity();
        transactionLog.setTerminalCode(event.getTerminalCode());
        transactionLog.setChannelCode(event.getChannelCode());
        transactionLog.setClientId(event.getClientId() != null ? event.getClientId() : event.getTerminalCode());//TODO:This is null. Check it.
        transactionLog.setCorrelationId(event.getCorrelationId());
        transactionLog.setClientCorrelationId(event.getClientCorrelationId());
        transactionLog.setClientFlowId(event.getClientFlowId());
        transactionLog.setServiceCode(event.getServiceCode());
        transactionLog.setNickname(event.getNickname());
        transactionLog.setUsername(event.getUsername());
        transactionLog.setDelegatorNickname(event.getDelegatorNickname());//TODO:This is null. Check it.
        transactionLog.setDelegatorUsername(event.getDelegatorUsername());//TODO:This is null. Check it.
        transactionLog.setMessageId(event.getMessageId());
        transactionLog.setHostAddress(event.getHostAddress());//TODO:This is null. Check it.
        transactionLog.setEventType(event.getEventType() != null ? event.getEventType().toString() : null);
        transactionLog.setFlowId(event.getFlowId());
        transactionLog.setVersion(event.getVersion());
        switch (event.getEventType()) {
            case INBOUND -> populateInboundLog(transactionLog, event);
            case OUTBOUND -> populateOutboundLog(transactionLog, event);
            case SERVICE_CALL -> populateServiceCallLog(transactionLog, event);
        }
        paymentTransferLog(event);
        return transactionLog;
    }

    private void paymentTransferLog(Event event) {
        if (event.getServiceCode() != null && event.getServiceCode().equalsIgnoreCase("")) {//TODO service code,complete this code snippet
            //  transactionLog.setAmount();//TODO from input
            //transactionLog.setCardNumber();//TODO from input
        }
    }

    private void populateInboundLog(TransactionLogEntity transactionLog, Event event) {
        if (event instanceof InboundEvent inboundEvent) {
            transactionLog.setDurationMills(inboundEvent.getDurationMillis());
            transactionLog.setMessageStatus(inboundEvent.getMessageStatus() != null ? inboundEvent.getMessageStatus().name() : null);
            Instant startTime = inboundEvent.getStartTime();
            Instant endTime = inboundEvent.getEndTime();
            setDate(transactionLog, startTime, endTime);
            MessageInput messageInput = inboundEvent.getMessageInput();
            transactionLog.setHostAddress(messageInput.getServerHost());
            if (messageInput instanceof HttpMessageInput httpMessageInput) {
                transactionLog.setUrl(httpMessageInput.getHttpUrl());
                transactionLog.setMethodType(httpMessageInput.getHttpMethod());
            }
        }
    }

    private void populateServiceCallLog(TransactionLogEntity transactionLog, Event event) {
        if (event instanceof ServiceEvent serviceEvent) {
            transactionLog.setMessageStatus(serviceEvent.getStatus() != null ? serviceEvent.getStatus().name() : null);
            transactionLog.setExceptionClassName(serviceEvent.getExceptionClassName());// TODO: The wrong exception class is being set because all exceptions are currently being set to the generic Exception type.
            transactionLog.setDurationMills(serviceEvent.getDurationMillis());
            Instant startTime = serviceEvent.getStartTime();
            Instant endTime = serviceEvent.getEndTime();
            setDate(transactionLog, startTime, endTime);
        }
    }

    private void setDate(TransactionLogEntity transactionLog, Instant startTime, Instant endTime) {
        Date startDate = startTime != null ? DateUtils.DateConverter.convertToDate(startTime) : null;
        transactionLog.setStartTime(startDate);
        Date endDate = endTime != null ? DateUtils.DateConverter.convertToDate(endTime) : null;
        transactionLog.setEndTime(endDate);
    }

    private void populateOutboundLog(TransactionLogEntity transactionLog, Event event) {
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

    public String createExceptionLog(Exception e, String msg) {
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
