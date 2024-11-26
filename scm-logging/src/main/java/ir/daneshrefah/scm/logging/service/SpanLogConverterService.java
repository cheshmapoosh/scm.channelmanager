package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.model.LogMessage;
import ir.daneshrefah.scm.logging.model.SpanModel;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SpanLogConverterService implements ConverterService {

    private final ObjectMapper objectMapper;

    public LogMessage convertToLogMessage(String msg) throws JsonProcessingException {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.readValue(msg, LogMessage.class);
    }

    @Override
    public LogTraceEntity convertToLogTraceEntity(LogMessage logMessage) {
        SpanModel spanModel = logMessage.getPayload();
        Map<String, String> attributes = spanModel.getAttributes();
        LogTraceEntity logTraceEntity = new LogTraceEntity();
        logTraceEntity.setChannelCode(attributes.get(LogAttribute.CHANNEL_CODE.getAttributeName()));
        logTraceEntity.setTerminalCode(attributes.get(LogAttribute.TERMINAL_CODE.getAttributeName()));
        logTraceEntity.setClientId(attributes.get(LogAttribute.CLIENT_ID.getAttributeName()));
        logTraceEntity.setCorrelationId(attributes.get(LogAttribute.CORRELATION_ID.getAttributeName()));
        logTraceEntity.setClientCorrelationId(attributes.get(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName()));
        logTraceEntity.setFlowId(attributes.get(LogAttribute.FLOW_ID.getAttributeName()));
        logTraceEntity.setMessageId(attributes.get(LogAttribute.MESSAGE_ID.getAttributeName()));
        logTraceEntity.setExceptionClassName(attributes.get(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName()));
        logTraceEntity.setEndPoint(attributes.get(LogAttribute.END_POINT.getAttributeName()) != null ? attributes.get(LogAttribute.END_POINT.getAttributeName()) : attributes.get(LogAttribute.URL_PATH.getAttributeName()));
        Integer statusCode = attributes.get(LogAttribute.HTTP_STATUS_CODE.getAttributeName()) != null ? Integer.valueOf(attributes.get(LogAttribute.HTTP_STATUS_CODE.getAttributeName())) : null;
        logTraceEntity.setStatusCode(statusCode);
        logTraceEntity.setVersion(attributes.get(LogAttribute.VERSION.getAttributeName()));
        if (spanModel.getStartEpochNanos() > 0) {
            logTraceEntity.setStartTime(new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getStartEpochNanos())));
        }
        if (spanModel.getEndEpochNanos() > 0) {
            logTraceEntity.setEndTime(new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getEndEpochNanos())));
        }
        logTraceEntity.setServiceCode(attributes.get(LogAttribute.SERVICE_CODE.getAttributeName()));
        logTraceEntity.setNickname(attributes.get(LogAttribute.NICKNAME.getAttributeName()));
        logTraceEntity.setUsername(attributes.get(LogAttribute.USERNAME.getAttributeName()));
        logTraceEntity.setDelegatorUsername(attributes.get(LogAttribute.DELEGATOR_USERNAME.getAttributeName()));
        logTraceEntity.setDelegatorNickname(attributes.get(LogAttribute.DELEGATOR_NICKNAME.getAttributeName()));
        logTraceEntity.setHostAddress(attributes.get(LogAttribute.HOST_ADDRESS.getAttributeName()));
        logTraceEntity.setMessageStatus(attributes.get(LogAttribute.MESSAGE_STATUS.getAttributeName()));
        logTraceEntity.setAccountNo(attributes.get(LogAttribute.ACCOUNT_NO.getAttributeName()));
        logTraceEntity.setCardNo(attributes.get(LogAttribute.CARD_NO.getAttributeName()));
        logTraceEntity.setAmount(attributes.get(LogAttribute.AMOUNT.getAttributeName()));
        logTraceEntity.setProviderCode(attributes.get(LogAttribute.PROVIDER_CODE.getAttributeName()));
        logTraceEntity.setProviderResponseCode(attributes.get(LogAttribute.PROVIDER_RESPONSE_CODE.getAttributeName()));
        logTraceEntity.setClientIpAddress(attributes.get(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName()));
        logTraceEntity.setTraceId(spanModel.getTraceId());
        logTraceEntity.setSpanId(spanModel.getSpanId());
        logTraceEntity.setSpanKind(spanModel.getKind());
        logTraceEntity.setSpanStatus(spanModel.getStatus().get("statusCode"));
        logTraceEntity.setParentSpanId(spanModel.getParentSpanId());
        logTraceEntity.setSpanName(spanModel.getName());
        logTraceEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        return logTraceEntity;
    }
}
