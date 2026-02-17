package ir.daneshrefah.scm.log.service;

import com.vdurmont.semver4j.Requirement;
import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.log.configuration.LogConditions;
import ir.daneshrefah.scm.common.log.entity.message.LogStatus;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.service.TransactionLogService;
import ir.daneshrefah.scm.log.model.Log;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.model.SpanModel;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@Conditional(LogConditions.TransactionLogTraceCondition.class)
public class TransactionLogConverterService implements ConverterService {

    private final TransactionLogService transactionLogService;
    private static final Integer TRANSACTION_TYPE_REQUEST = 1;
    private static final Integer TRANSACTION_TYPE_RESPONSE = 2;
    @Value("${scm.log.transactionLogConverter.chunkSize:2048}")
    private Integer CHUNK_SIZE;
    private final Requirement versionRequirement;

    public TransactionLogConverterService(TransactionLogService transactionLogService,
                                          @Value("${scm.log.transactionLogConverter.versionRequirement:#{null}}") String versionRequirement) {
        this.transactionLogService = transactionLogService;
        if (versionRequirement != null && !versionRequirement.isEmpty()) {
            this.versionRequirement = Requirement.buildNPM(versionRequirement);
        } else {
            this.versionRequirement = Requirement.buildNPM("*");
        }
    }

    @Override
    public boolean supports(LogMessage logMessage) {
//        String version = logMessage.getPayload().getAttributes().get(LogAttribute.VERSION.getAttributeName());
//        return   StringUtils.isNotBlank(logMessage.getPayload().getAttributes().get("scm-source")) || versionRequirement.isSatisfiedBy(version);
        return true;
    }

    @PostConstruct
    public void init() {
        log.info(">>> TransactionLogConverterService successfully initialized");
    }

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
        if (StringUtils.isEmpty(attributes.get("scm-source"))) {
            entities.add(buildTransactionLogEntity(spanModel, true));
            String responseStr = attributes.get(LogAttribute.TRANSACTION_TYPE_RESPONSE.getAttributeName());
            if (StringUtils.isNotBlank(responseStr) && Integer.valueOf(responseStr).equals(TRANSACTION_TYPE_RESPONSE)) {
                entities.add(buildTransactionLogEntity(spanModel, false));
            }
        } else if (StringUtils.isNotBlank(attributes.get("scm-source"))) {
            entities.add(buildScmUaaTransactionLogEntity(spanModel, true));
            String responseStr = attributes.get("responseBody");
            if (StringUtils.isNotBlank(responseStr)) {
                entities.add(buildScmUaaTransactionLogEntity(spanModel, false));
            }

        }
        return entities;
    }

    public TransactionLogEntity buildScmUaaTransactionLogEntity(SpanModel spanModel, Boolean isRequest) {
        Map<String, String> attributes = spanModel.getAttributes();
        TransactionLogEntity transactionLogEntity = new TransactionLogEntity();
        LocalDateTime ldt = LocalDateTime.parse(required(attributes, "transactionDate"));
        ldt = ldt.withNano((ldt.getNano() / 1_000) * 1_000);
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date logTime = Date.from(instant);
        transactionLogEntity.setArchiveNo(ArchiveUtils.calculateTenDaysArchiveNo(logTime));
        if (isRequest) {
            transactionLogEntity.setTransactionType(1);
            transactionLogEntity.setPayload(attributes.get("requestBody"));
            transactionLogEntity.setTransactionStateId(7);
            transactionLogEntity.setLogTime(logTime);
        } else {
            transactionLogEntity.setDocNo(attributes.get(LogAttribute.DOC_NO.getAttributeName()));
            transactionLogEntity.setServerCode(attributes.get((LogAttribute.PROVIDER_CODE.getAttributeName())));
            String stCode = attributes.get("responseStatus");
            String resultStCode = LogStatus.RESPONSE_FROM_CHANNEL.name().equals(stCode) ? "0" : "100";
            transactionLogEntity.setStatusCode(resultStCode);
            transactionLogEntity.setTransactionType(2);
            transactionLogEntity.setPayload(attributes.get("responseBody"));
            transactionLogEntity.setTransactionStateId(8);
            long endEpochNanos = spanModel.getEndEpochNanos();
            Instant instantRes = Instant.ofEpochSecond(
                    endEpochNanos / 1_000_000_000L,   // seconds
                    endEpochNanos % 1_000_000_000L    // nanoseconds part
            );
            transactionLogEntity.setLogTime(Date.from(instantRes ));
        }
        //transactionLogEntity.setServerException(getExceptionClassName(attributes));

        if(attributes.get("serviceType") != null && "loginRequest".equals(attributes.get("serviceType"))){
               transactionLogEntity.setEbServiceId(34);
        }
        else {
            transactionLogEntity.setEbServiceId(convertToInteger(attributes, LogAttribute.SERVICE_ID));
        }

        String channelCode = attributes.get("clientType");
        if ("PWA".equals(channelCode)) {
            channelCode = TerminalType.MB.name();
        }
        transactionLogEntity.setChannelId(TerminalType.findByTerminalCode(channelCode).getLegacyTerminalId().intValue());
        transactionLogEntity.setUsername(attributes.get("nickName"));
        transactionLogEntity.setMessageSequenceId(attributes.get("correlationId"));

        transactionLogEntity.setClientDate(getClientTime(attributes));
        transactionLogEntity.setClientIPAddress(attributes.get("ip"));
        return transactionLogEntity;
    }


    public TransactionLogEntity buildTransactionLogEntity(SpanModel spanModel, Boolean isRequest) {
        Map<String, String> attributes = spanModel.getAttributes();
        TransactionLogEntity transactionLogEntity = new TransactionLogEntity();
        if (isRequest) {
            transactionLogEntity.setTransactionType(TRANSACTION_TYPE_REQUEST);
            transactionLogEntity.setPayload(getMessage(attributes, LogAttribute.MESSAGE_REQUEST));
        } else {
            transactionLogEntity.setDocNo(attributes.get(LogAttribute.DOC_NO.getAttributeName()));
            transactionLogEntity.setServerCode(attributes.get((LogAttribute.PROVIDER_CODE.getAttributeName())));
            transactionLogEntity.setStatusCode(attributes.get(LogAttribute.STATUS_CODE.getAttributeName()));
            transactionLogEntity.setTransactionType(TRANSACTION_TYPE_RESPONSE);
            transactionLogEntity.setPayload(getMessage(attributes, LogAttribute.MESSAGE_RESPONSE));
        }
        transactionLogEntity.setServerException(getExceptionClassName(attributes));
        Date logTime = getLogTime(spanModel, isRequest);
        transactionLogEntity.setArchiveNo(ArchiveUtils.calculateTenDaysArchiveNo(logTime));
        transactionLogEntity.setEbServiceId(convertToInteger(attributes, LogAttribute.SERVICE_ID));
        transactionLogEntity.setChannelId(convertToInteger(attributes, LogAttribute.CHANNEL_ID));
        transactionLogEntity.setTransactionStateId(convertToInteger(attributes, LogAttribute.TRANSACTION_STATE_ID));
        transactionLogEntity.setDuplicate(convertToInteger(attributes, LogAttribute.DUPLICATE));
        transactionLogEntity.setCspChannelId(convertToInteger(attributes, LogAttribute.CSP_CHANNEL_ID));
        transactionLogEntity.setCspUsername(attributes.get(LogAttribute.DELEGATOR_USERNAME.getAttributeName()));
        transactionLogEntity.setUsername(attributes.get(LogAttribute.USERNAME.getAttributeName()));
        transactionLogEntity.setAccountNo(attributes.get(LogAttribute.ACCOUNT_NO.getAttributeName()));
        transactionLogEntity.setMessageSequenceId(getMessageSequenceId(attributes));
        transactionLogEntity.setLogTime(logTime);
        transactionLogEntity.setDescription(attributes.get(LogAttribute.DESCRIPTION.getAttributeName()));
        transactionLogEntity.setTerminalType(attributes.get(LogAttribute.TERMINAL_TYPE.getAttributeName()));
        transactionLogEntity.setInterBank(convertToBoolean(attributes, LogAttribute.INTER_BANK));
        transactionLogEntity.setAmount(convertToLong(attributes, LogAttribute.AMOUNT));
        transactionLogEntity.setTerminalId(attributes.get(LogAttribute.TERMINAL_Id.getAttributeName()));
        transactionLogEntity.setCardNo(attributes.get(LogAttribute.CARD_NO.getAttributeName()));
        transactionLogEntity.setClientDate(getClientTime(attributes));
        transactionLogEntity.setExternalSequenceId(StringUtils.left(attributes.get(LogAttribute.EXTERNAL_SEQUENCE_ID.getAttributeName()), 32));
        transactionLogEntity.setOriginalSequenceId(StringUtils.left(attributes.get(LogAttribute.ORIGINAL_SEQUENCE_ID.getAttributeName()), 32));
        transactionLogEntity.setDestination(attributes.get(LogAttribute.DESTINATION.getAttributeName()));
        transactionLogEntity.setClientIPAddress(attributes.get(LogAttribute.CLIENT_IP_ADDRESS.getAttributeName()));
        return transactionLogEntity;
    }

    private static Integer getTransactionType(Map<String, String> attributes, LogAttribute logAttribute) {
        String transactionType = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(transactionType) && StringUtils.isNumeric(transactionType)) {
            return Integer.valueOf(transactionType);
        }
        return null;
    }

    private String getMessage(Map<String, String> attributes, LogAttribute logAttribute) {
        String message = attributes.get(logAttribute.getAttributeName());
        if (StringUtils.isNotBlank(message)) {
            return truncateUtf8(message.replaceAll(" {2,}", " "));
        }
        return null;
    }

    private String truncateUtf8(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= CHUNK_SIZE) {
            return value;
        }
        int len = CHUNK_SIZE;
        while (len > 0 && (bytes[len] & 0xC0) == 0x80) {
            len--;
        }
        return new String(bytes, 0, len, StandardCharsets.UTF_8);
    }

    private static String getMessageSequenceId(Map<String, String> attributes) {
        return StringUtils.left(attributes.get(LogAttribute.MESSAGE_ID.getAttributeName()), 32);
    }

    private static Date getLogTime(SpanModel spanModel, Boolean isRequest) {
        Map<String, String> attributes = spanModel.getAttributes();
        Date date = null;
        if (isRequest) {
            String startTime = attributes.get(LogAttribute.START_TIME.getAttributeName());
            if (StringUtils.isNotBlank(startTime) && StringUtils.isNumeric(startTime)) {
                date = convertToDate(startTime);
            }
        } else {
            String endTime = attributes.get(LogAttribute.END_TIME.getAttributeName());
            if (StringUtils.isNotBlank(endTime) && StringUtils.isNumeric(endTime)) {
                date = convertToDate(endTime);
            }
        }
        if (date == null) {
            if (spanModel.getStartEpochNanos() > 0) {
                date = new Date(TimeUnit.NANOSECONDS.toMillis(spanModel.getStartEpochNanos()));
            }
        }
        return date;
    }

    private static Date convertToDate(String date) {
        switch (date.length()) {
            case 10:
                return new Date(TimeUnit.SECONDS.toMillis(Long.parseLong(date)));
            case 13:
                return new Date(TimeUnit.MILLISECONDS.toMillis(Long.parseLong(date)));
            case 16:
                return new Date(TimeUnit.MICROSECONDS.toMillis(Long.parseLong(date)));
            case 19:
                return new Date(TimeUnit.NANOSECONDS.toMillis(Long.parseLong(date)));
            default:
                return null;
        }
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


    private static String getExceptionClassName(Map<String, String> attributes) {
        String exceptionClassName = attributes.get(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName());
        int maxLength = 255;
        if (exceptionClassName != null && exceptionClassName.length() > maxLength) {
            exceptionClassName = exceptionClassName.substring(exceptionClassName.length() - maxLength);
        }
        return exceptionClassName;
    }

    private static String required(Map<String, String> attrs, String key) {
        String value = attrs.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }
}
