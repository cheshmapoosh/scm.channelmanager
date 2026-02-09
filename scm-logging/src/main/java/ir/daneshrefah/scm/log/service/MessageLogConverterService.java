package ir.daneshrefah.scm.log.service;

import com.vdurmont.semver4j.Requirement;
import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.log.configuration.LogConditions;
import ir.daneshrefah.scm.common.log.entity.message.LogStatus;
import ir.daneshrefah.scm.common.log.entity.message.MessageLogEntity;
import ir.daneshrefah.scm.common.log.service.MessageLogService;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.model.SpanModel;
import ir.daneshrefah.scm.utils.constant.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
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
public class MessageLogConverterService implements ConverterService {

    private final MessageLogService messageLogService;
    private static final Integer TRANSACTION_TYPE_REQUEST = 1;
    private static final Integer TRANSACTION_TYPE_RESPONSE = 2;
    private final Requirement versionRequirement;

    public MessageLogConverterService(MessageLogService messageLogService,
                                      @Value("${scm.log.transactionLogConverter.versionRequirement:#{null}}") String versionRequirement) {
        this.messageLogService = messageLogService;
        if (versionRequirement != null && !versionRequirement.isEmpty()) {
            this.versionRequirement = Requirement.buildNPM(versionRequirement);
        } else {
            this.versionRequirement = Requirement.buildNPM("*");
        }
    }

    @Override
    public boolean supports(LogMessage logMessage) {
        Map<String,String> attributes =logMessage.getPayload().getAttributes();
        String version = attributes.get(LogAttribute.VERSION.getAttributeName());
        return  StringUtils.isNotBlank(attributes.get("scm-source"));

    }

    @PostConstruct
    public void init() {
        log.info(">>> TransactionLogConverterService successfully initialized");
    }

    @Override
    public void convertAndPersist(LogMessage logMessage) throws Exception {
        if (logMessage.getPayload() != null && logMessage.getPayload().getKind() != null && !logMessage.getPayload().getKind().equalsIgnoreCase("CLIENT")) {
            List<MessageLogEntity> entities = convert(logMessage);
            messageLogService.saveAll(entities);
        }
    }

    private List<MessageLogEntity> convert(LogMessage logMessage) {
        SpanModel spanModel = logMessage.getPayload();
        Map<String, String> attributes = spanModel.getAttributes();
        List<MessageLogEntity> entities = new ArrayList<>();
        entities.add(buildRequestMessageLogEntity(spanModel));
        String responseStr = attributes.get("responseBody");
        if (StringUtils.isNotBlank(responseStr)) {
            entities.add(buildResponseMessageLogEntity(spanModel));
        }
        return entities;
    }

    public MessageLogEntity buildRequestMessageLogEntity(SpanModel spanModel) {
        Map<String, String> attributes = spanModel.getAttributes();
        MessageLogEntity messageLogsEntity = new MessageLogEntity();

        messageLogsEntity.setAccessParam(attributes.get("accessParam"));
        messageLogsEntity.setBody(required(attributes,"requestBody"));
        messageLogsEntity.setCorrelationId(required(attributes,"correlationId"));
        messageLogsEntity.setClientType(attributes.get("clientType"));
         messageLogsEntity.setRespAllocatedTime(0);
        messageLogsEntity.setRealUsername(required(attributes,"username"));
        messageLogsEntity.setIp(required(attributes,"ip"));
        messageLogsEntity.setUsername(required(attributes,"nickName"));
        messageLogsEntity.setServiceType("loginResponse");
        messageLogsEntity.setStatus(LogStatus.valueOf(required(attributes,"status")));

        LocalDateTime ldt = LocalDateTime.parse(required(attributes,"transactionDate"));
        messageLogsEntity.setTransactionDate(ldt);

        return messageLogsEntity;
    }



    public MessageLogEntity buildResponseMessageLogEntity(SpanModel spanModel) {
        Map<String, String> attributes = spanModel.getAttributes();
        MessageLogEntity messageLogsEntity = new MessageLogEntity();
        messageLogsEntity.setAmount(attributes.get("amount") != null ? Long.valueOf(attributes.get("amount")):null);
        messageLogsEntity.setBody(required(attributes,"responseBody"));
        messageLogsEntity.setCorrelationId(required(attributes,"correlationId"));//not null
        messageLogsEntity.setRespAllocatedTime(Long.parseLong(attributes.get("allocatedTime")));
        messageLogsEntity.setRealUsername(required(attributes,"username"));
        messageLogsEntity.setUsername(required(attributes,"nickName"));
        messageLogsEntity.setServiceType(required(attributes,"serviceType"));
        messageLogsEntity.setStatus(LogStatus.valueOf(required(attributes,"responseStatus")));

        LocalDateTime ldt = LocalDateTime.parse(attributes.get("transactionDate"));
        messageLogsEntity.setTransactionDate(ldt);

        return messageLogsEntity;
    }


    private static String required(Map<String, String> attrs, String key) {
        String value = attrs.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }



    private static String getMessageSequenceId(Map<String, String> attributes) {
        return attributes.get(LogAttribute.MESSAGE_ID.getAttributeName());
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
