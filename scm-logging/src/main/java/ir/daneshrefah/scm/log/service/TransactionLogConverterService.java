package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.service.TransactionLogService;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.model.SpanModel;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "scm.log.transactionLogConverter.enabled", havingValue = "true", matchIfMissing = true)
public class TransactionLogConverterService implements ConverterService {

    private final TransactionLogService transactionLogService;
    private static final Integer TRANSACTION_TYPE_REQUEST = 1;
    private static final Integer TRANSACTION_TYPE_RESPONSE = 2;
    private static final int CHUNK_SIZE = 2048;

    @Override
    public void convertAndPersist(LogMessage logMessage) throws Exception {
        if (logMessage.getPayload() != null && logMessage.getPayload().getKind() != null && !logMessage.getPayload().getKind().equalsIgnoreCase("CLIENT")) {
            List<TransactionLogEntity> entities = convert(logMessage);
            transactionLogService.saveAll(entities);
        }
    }

    private List<TransactionLogEntity> convert(LogMessage logMessage) {
        SpanModel spanModel = logMessage.getPayload();
        Map<String, String> attributes = spanModel.getAttributes();
        List<TransactionLogEntity> entities = new ArrayList<>();
        entities.add(buildTransactionLogEntity(spanModel, true));
        String responseStr = attributes.get(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName());
        if (StringUtils.isNotBlank(responseStr) && Integer.valueOf(responseStr).equals(TRANSACTION_TYPE_RESPONSE)) {
            entities.add(buildTransactionLogEntity(spanModel, false));
        }
        return entities;
    }

    public TransactionLogEntity buildTransactionLogEntity(SpanModel spanModel, Boolean isRequest) {
        Map<String, String> attributes = spanModel.getAttributes();
        TransactionLogEntity transactionLogEntity = new TransactionLogEntity();
        if (isRequest) {
            transactionLogEntity.setTransactionType(getTransactionType(attributes, LogAttribute.TRANSACTION_TYPE_REQUEST));
            transactionLogEntity.setPayload(getMessage(attributes, LogAttribute.MESSAGE_REQUEST));
        } else {
            transactionLogEntity.setDocNo(attributes.get(LogAttribute.DOC_NO.getAttributeName()));
            transactionLogEntity.setServerCode(attributes.get((LogAttribute.PROVIDER_CODE.getAttributeName())));
            transactionLogEntity.setStatusCode(attributes.get(LogAttribute.STATUS_CODE.getAttributeName()));
            transactionLogEntity.setTransactionType(getTransactionType(attributes, LogAttribute.TRANSACTION_TYPE_RESPONSE));
            transactionLogEntity.setPayload(getMessage(attributes, LogAttribute.MESSAGE_RESPONSE));
        }
        transactionLogEntity.setServerException(getExceptionClassName(attributes));
        transactionLogEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        transactionLogEntity.setEbServiceId(convertToInteger(attributes, LogAttribute.SERVICE_ID));
        transactionLogEntity.setChannelId(convertToInteger(attributes, LogAttribute.CHANNEL_ID));
        transactionLogEntity.setTransactionStateId(convertToInteger(attributes, LogAttribute.TRANSACTION_STATE_ID));
        transactionLogEntity.setDuplicate(convertToInteger(attributes, LogAttribute.DUPLICATE));
        transactionLogEntity.setCspChannelId(convertToInteger(attributes, LogAttribute.CSP_CHANNEL_ID));
        transactionLogEntity.setCspUsername(attributes.get(LogAttribute.DELEGATOR_USERNAME.getAttributeName()));
        transactionLogEntity.setUsername(attributes.get(LogAttribute.USERNAME.getAttributeName()));
        transactionLogEntity.setAccountNo(attributes.get(LogAttribute.ACCOUNT_NO.getAttributeName()));
        transactionLogEntity.setMessageSequenceId(getMessageSequenceId(attributes));
        transactionLogEntity.setLogTime(getLogTime(spanModel, isRequest));
        transactionLogEntity.setDescription(attributes.get(LogAttribute.DESCRIPTION.getAttributeName()));
        transactionLogEntity.setTerminalType(attributes.get(LogAttribute.TERMINAL_TYPE.getAttributeName()));
        transactionLogEntity.setInterBank(convertToBoolean(attributes, LogAttribute.INTER_BANK));
        transactionLogEntity.setAmount(convertToLong(attributes, LogAttribute.AMOUNT));
        transactionLogEntity.setTerminalId(attributes.get(LogAttribute.TERMINAL_Id.getAttributeName()));
        transactionLogEntity.setCardNo(attributes.get(LogAttribute.CARD_NO.getAttributeName()));
        transactionLogEntity.setClientDate(getClientTime(attributes));
        transactionLogEntity.setExternalSequenceId(attributes.get(LogAttribute.EXTERNAL_SEQUENCE_ID.getAttributeName()));
        transactionLogEntity.setOriginalSequenceId(attributes.get(LogAttribute.ORIGINAL_SEQUENCE_ID.getAttributeName()));
        transactionLogEntity.setDestination(attributes.get(LogAttribute.DESTINATION.getAttributeName()));
        setIpAddress(attributes, transactionLogEntity);
        return transactionLogEntity;
    }

    private static Integer getTransactionType(Map<String, String> attributes, LogAttribute logAttribute) {
        String transactionType = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(transactionType) && StringUtils.isNumeric(transactionType)) {
            return Integer.valueOf(transactionType);
        }
        return null;
    }

    private static String getMessage(Map<String, String> attributes, LogAttribute logAttribute) {
        String message = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(message)) {
            return truncateUtf8(message.replaceAll("\\s+", ""));
        }
        return null;
    }

    private static String truncateUtf8(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= CHUNK_SIZE) {
            return value;
        }
        int endIndex = value.length();
        while (new String(value.substring(0, endIndex).getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8).length > CHUNK_SIZE) {
            endIndex--;
        }
        return value.substring(0, endIndex);
    }

    private static String getMessageSequenceId(Map<String, String> attributes) {
        return attributes.get(LogAttribute.MESSAGE_ID.getAttributeName());
    }

    private static Date getLogTime(SpanModel spanModel, Boolean isRequest) {
        Map<String, String> attributes = spanModel.getAttributes();
        Date date = null;
        if (isRequest) {
            String startTime = attributes.get(LogAttribute.START_TIME.getAttributeName());
            if (StringUtils.isNotBlank(startTime) && StringUtils.isNumeric(startTime)) {
                date = new Date(TimeUnit.NANOSECONDS.toMillis(Long.parseLong(startTime)));
            }
        } else {
            String endTime = attributes.get(LogAttribute.END_TIME.getAttributeName());
            if (StringUtils.isNotBlank(endTime) && StringUtils.isNumeric(endTime)) {
                date = new Date(TimeUnit.NANOSECONDS.toMillis(Long.parseLong(endTime)));
            }
        }
        if (date == null) {
            if (spanModel.getStartEpochNanos() > 0) {
                date = new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getStartEpochNanos()));
            }
        }
        return date;
    }

    private static Date getClientTime(Map<String, String> attributes) {
        Date date = null;
        String logTime = attributes.get(LogAttribute.LOG_TIME.getAttributeName());
        if (StringUtils.isNotBlank(logTime) && StringUtils.isNumeric(logTime)) {
            date = new Date(TimeUnit.SECONDS.toMillis(Long.parseLong(logTime)));
        }
        return date;
    }

    private static Integer convertToInteger(Map<String, String> attributes, LogAttribute logAttribute) {
        String value = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(value) && StringUtils.isNumeric(value)) {
            return Integer.valueOf(value);
        }
        return null;
    }

    private static Long convertToLong(Map<String, String> attributes, LogAttribute logAttribute) {
        String value = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(value) && StringUtils.isNumeric(value)) {
            return Long.valueOf(value);
        }
        return null;
    }

    private static Boolean convertToBoolean(Map<String, String> attributes, LogAttribute logAttribute) {
        String value = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(value)) {
            return Boolean.valueOf(value);
        }
        return null;
    }

    private void setIpAddress(Map<String, String> attributes, TransactionLogEntity transactionLogEntity) {
        transactionLogEntity.setClientIPAddress(attributes.get(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName()));
        transactionLogEntity.setClientPhoneNumber(attributes.get(LogAttribute.CLIENT_PHONE_NUMBER.getAttributeName()));
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
