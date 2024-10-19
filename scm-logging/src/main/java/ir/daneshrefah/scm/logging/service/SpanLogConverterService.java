package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.logging.constant.LogAttribute;
import ir.daneshrefah.scm.logging.entity.TransactionLogEntity;
import ir.daneshrefah.scm.logging.model.LogMessage;
import ir.daneshrefah.scm.logging.model.SpanModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SpanLogConverterService implements ConverterServiceImpl {

    private final ObjectMapper objectMapper;
    public LogMessage convertToLogMessage(String msg) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.readValue(msg, LogMessage.class);
    }

    @Override
    public TransactionLogEntity convertToTransactionLogEntity(LogMessage logMessage) {
        TransactionLogEntity transactionLogEntity = new TransactionLogEntity();
        SpanModel spanModel = logMessage.getPayload();
        Map<String, String> attributes = spanModel.getAttributes();
        transactionLogEntity.setChannelCode(attributes.get(LogAttribute.CHANNEL_CODE.getAttributeName()));
        transactionLogEntity.setTerminalCode(attributes.get(LogAttribute.TERMINAL_CODE.getAttributeName()));
        transactionLogEntity.setClientId(attributes.get(LogAttribute.CLIENT_ID.getAttributeName()));
        transactionLogEntity.setCorrelationId(attributes.get(LogAttribute.CORRELATION_ID.getAttributeName()));
        transactionLogEntity.setClientCorrelationId(attributes.get(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName()));
        transactionLogEntity.setClientFlowId(attributes.get(LogAttribute.CLIENT_FLOW_ID.getAttributeName()));
        transactionLogEntity.setFlowId(attributes.get(LogAttribute.FLOW_ID.getAttributeName()));
        transactionLogEntity.setMessageId(attributes.get(LogAttribute.MESSAGE_ID.getAttributeName()));
        transactionLogEntity.setParentMessageId(attributes.get(LogAttribute.PARENT_MESSAGE_ID.getAttributeName()));
        transactionLogEntity.setExceptionClassName(attributes.get(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName()));
        transactionLogEntity.setEndPoint(attributes.get(LogAttribute.END_POINT.getAttributeName()));
        transactionLogEntity.setMethodType(attributes.get(LogAttribute.METHOD_TYPE.getAttributeName()));
        String statusCodeStr = attributes.get(LogAttribute.RESPONSE_STATUS_CODE.getAttributeName());
        Integer statusCode = statusCodeStr != null ? Integer.valueOf(statusCodeStr) : null;
        transactionLogEntity.setStatusCode(statusCode);
        transactionLogEntity.setVersion(attributes.get(LogAttribute.VERSION.getAttributeName()));
        if (spanModel.getStartEpochNanos() > 0) {
            transactionLogEntity.setStartTime(new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getStartEpochNanos())));
        }
        if (spanModel.getEndEpochNanos() > 0) {
            transactionLogEntity.setEndTime(new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getEndEpochNanos())));
        }
        if (spanModel.getStartEpochNanos() > 0 && spanModel.getEndEpochNanos() > 0) {
            long duration = spanModel.getEndEpochNanos() - spanModel.getStartEpochNanos() ;
            transactionLogEntity.setDurationMills(TimeUnit.NANOSECONDS.toMillis(duration));
        }
        transactionLogEntity.setServiceCode(attributes.get(LogAttribute.SERVICE_CODE.getAttributeName()));
        transactionLogEntity.setNickname(attributes.get(LogAttribute.NICKNAME.getAttributeName()));
        transactionLogEntity.setUsername(attributes.get(LogAttribute.USERNAME.getAttributeName()));
        transactionLogEntity.setDelegatorUsername(attributes.get(LogAttribute.DELEGATOR_USERNAME.getAttributeName()));
        transactionLogEntity.setDelegatorNickname(attributes.get(LogAttribute.DELEGATOR_NICKNAME.getAttributeName()));
        transactionLogEntity.setHostAddress(attributes.get(LogAttribute.HOST_ADDRESS.getAttributeName()));
        transactionLogEntity.setMessageStatus(attributes.get(LogAttribute.MESSAGE_STATUS.getAttributeName()));
        transactionLogEntity.setAccountNo(attributes.get(LogAttribute.ACCOUNT_NO.getAttributeName()));
        transactionLogEntity.setCardNo(attributes.get(LogAttribute.CARD_NO.getAttributeName()));
        transactionLogEntity.setAmount(attributes.get(LogAttribute.AMOUNT.getAttributeName()));
        transactionLogEntity.setProviderCode(attributes.get(LogAttribute.PROVIDER_CODE.getAttributeName()));
        transactionLogEntity.setProviderResponseCode(attributes.get(LogAttribute.PROVIDER_RESPONSE_CODE.getAttributeName()));
        transactionLogEntity.setTraceId(spanModel.getTraceId());
        transactionLogEntity.setSpanId(spanModel.getSpanId());
        transactionLogEntity.setSpanKind(spanModel.getKind());
        transactionLogEntity.setSpanStatus(spanModel.getStatus().get("statusCode"));
        transactionLogEntity.setParentSpanId(spanModel.getParentSpanId());
        transactionLogEntity.setSpanName(spanModel.getName());
        return transactionLogEntity;
    }
}
