package ir.daneshrefah.scm.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.log.entity.logging.LogPrimaryKey;
import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.model.SpanModel;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SpanLogConverterService {

    private final ObjectMapper objectMapper;
    @Value("${scm.log.chunkSize:5600}")
    private int chunkSize;

    public List<LogTraceEntity> mapToLogTraceEntity(LogMessage logMessage) throws Exception {
        List<LogTraceEntity> logTraceEntities = new ArrayList<>();
        String payload = objectMapper.writeValueAsString(logMessage);
        List<String> splitPayload = splitPayload(payload);
        for (int i = 0; i < splitPayload.size(); i++) {
            LogTraceEntity logTraceEntity = buildLogTraceEntity(logMessage, i);
            logTraceEntity.setPayload(splitPayload.get(i));
            logTraceEntities.add(logTraceEntity);
        }
        return logTraceEntities;
    }

    public LogTraceEntity buildLogTraceEntity(LogMessage logMessage, int rowId) {
        SpanModel spanModel = logMessage.getPayload();
        ValidationUtils.checkBlankString(spanModel.getSpanId(), () -> new MissingRequiredInputException("spainId"));
        ValidationUtils.checkBlankString(spanModel.getTraceId(), () -> new MissingRequiredInputException("traceId"));
        LogTraceEntity logTraceEntity = new LogTraceEntity();
        Map<String, String> attributes = spanModel.getAttributes();
        LogPrimaryKey logPrimaryKey = new LogPrimaryKey(spanModel.getTraceId(), spanModel.getSpanId(), rowId);
        logTraceEntity.setLogPrimaryKey(logPrimaryKey);
        logTraceEntity.setChannelCode(attributes.get(LogAttribute.CHANNEL_CODE.getAttributeName()));
        logTraceEntity.setTerminalCode(attributes.get(LogAttribute.TERMINAL_CODE.getAttributeName()));
        logTraceEntity.setClientId(attributes.get(LogAttribute.CLIENT_ID.getAttributeName()));
        logTraceEntity.setCorrelationId(attributes.get(LogAttribute.CORRELATION_ID.getAttributeName()));
        logTraceEntity.setClientCorrelationId(attributes.get(LogAttribute.CLIENT_CORRELATION_ID.getAttributeName()));
        logTraceEntity.setFlowId(attributes.get(LogAttribute.FLOW_ID.getAttributeName()));
        logTraceEntity.setMessageId(attributes.get(LogAttribute.MESSAGE_ID.getAttributeName()));
        logTraceEntity.setExceptionClassName(getExceptionClassName(attributes));
        logTraceEntity.setEndPoint(attributes.get(LogAttribute.END_POINT.getAttributeName()) != null ? attributes.get(LogAttribute.END_POINT.getAttributeName()) : attributes.get(LogAttribute.URL_PATH.getAttributeName()));
        logTraceEntity.setStatusCode(getStatusCode(attributes));
        logTraceEntity.setVersion(attributes.get(LogAttribute.VERSION.getAttributeName()));
        Date startTime = getStartTime(spanModel);
        logTraceEntity.setStartTime(startTime);
        logTraceEntity.setEndTime(getEndTime(spanModel));
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
        logTraceEntity.setClientIpAddress(getClientIpAddress(attributes));
        logTraceEntity.setSpanKind(spanModel.getKind());
        logTraceEntity.setSpanStatus(spanModel.getStatus().get(LogAttribute.STATUS_CODE.getAttributeName()));
        logTraceEntity.setParentSpanId(spanModel.getParentSpanId());
        logTraceEntity.setSpanName(spanModel.getName());
        logTraceEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo(startTime));
        return logTraceEntity;
    }

    private static Integer getStatusCode(Map<String, String> attributes) {
        return attributes.get(LogAttribute.HTTP_STATUS_CODE.getAttributeName()) != null ? Integer.valueOf(attributes.get(LogAttribute.HTTP_STATUS_CODE.getAttributeName())) : null;
    }

    private static String getClientIpAddress(Map<String, String> attributes) {
        return attributes.get(LogAttribute.CLIENT_IP_ADDRESS.getAttributeName());
    }

    private Date getStartTime(SpanModel spanModel) {
        Date date = null;
        String startTime = spanModel.getAttributes().get(LogAttribute.START_TIME.getAttributeName());
        if (StringUtils.isNotBlank(startTime) && StringUtils.isNumeric(startTime)) {
            date = DateUtils.DateConverter.convertToDate(new Timestamp(Long.parseLong(startTime)));
        } else if (spanModel.getStartEpochNanos() > 0) {
            date = new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getStartEpochNanos()));
        }
        return date;
    }

    private Date getEndTime(SpanModel spanModel) {
        Date date = null;
        String endTime = spanModel.getAttributes().get(LogAttribute.END_TIME.getAttributeName());
        if (endTime != null) {
            date = DateUtils.DateConverter.convertToDate(new Timestamp(Long.parseLong(endTime)));
        } else if (spanModel.getEndEpochNanos() > 0) {
            date = new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getEndEpochNanos()));
        }
        return date;
    }

//    private List<String> splitPayload(String payload) {
//        List<String> parts = new ArrayList<>();
//        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
//        int start = 0;
//        while (start < payloadBytes.length) {
//            int end = Math.min(start + chunkSize, payloadBytes.length);
//            byte[] chunk = new byte[end - start];
//            System.arraycopy(payloadBytes, start, chunk, 0, end - start);
//            String chunkStr = new String(chunk, StandardCharsets.UTF_8);
//            parts.add(chunkStr);
//            start = end;
//        }
//        return parts;
//    }

    private List<String> splitPayload(String payload) {
        List<String> parts = new ArrayList<>();
        CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder();
        int start = 0;
        int payloadLength = payload.length();
        while (start < payloadLength) {
            int end = start;
            ByteBuffer byteBuffer = ByteBuffer.allocate(chunkSize);
            encoder.reset();
            while (end < payloadLength) {
                char c = payload.charAt(end);
                CharBuffer charBuffer = CharBuffer.wrap(new char[]{c});
                CoderResult result = encoder.encode(charBuffer, byteBuffer, true);
                if (result.isOverflow()) {
                    break;
                }
                end++;
            }
            parts.add(payload.substring(start, end));
            start = end;
        }
        return parts;
    }

    private static String getExceptionClassName(Map<String, String> attributes) {
        String exceptionClassName = attributes.get(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName());
        int maxLength = 255;
        if (exceptionClassName != null && exceptionClassName.length() > maxLength) {
            exceptionClassName = exceptionClassName.substring(exceptionClassName.length() - maxLength);
        }
        return exceptionClassName;
    }
}
