package ir.daneshrefah.scm.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.log.entity.transaction.TransactionLogEntity;
import ir.daneshrefah.scm.common.log.service.TransactionLogService;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.model.SpanModel;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionLogConverterService implements ConverterService {

    private final TransactionLogService transactionLogService;
    private final ObjectMapper objectMapper;

    @Override
    public void convertAndPersist(String message) throws Exception {
        List<TransactionLogEntity> entities = convert(message);
        transactionLogService.saveAll(entities);
    }

    private List<TransactionLogEntity> convert(String msg) throws Exception {
        LogMessage logMessage = deserializeLogMessage(msg);
        return List.of(buildTransactionLogEntity(logMessage));
    }

    public TransactionLogEntity buildTransactionLogEntity(LogMessage logMessage) {
        SpanModel spanModel = logMessage.getPayload();
        Map<String, String> attributes = spanModel.getAttributes();
        TransactionLogEntity transactionLogEntity = new TransactionLogEntity();
        transactionLogEntity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo());
        transactionLogEntity.setEbServiceId(convertToInteger(attributes, LogAttribute.SERVICE_ID));
        transactionLogEntity.setChannelId(convertToInteger(attributes, LogAttribute.CHANNEL_ID));
        transactionLogEntity.setTransactionStateId(convertToInteger(attributes, LogAttribute.TRANSACTION_STATE_ID));
        transactionLogEntity.setDuplicate(convertToInteger(attributes, LogAttribute.DUPLICATE));
        transactionLogEntity.setCspChannelId(convertToInteger(attributes, LogAttribute.CSP_CHANNEL_ID));
        transactionLogEntity.setCspUsername(attributes.get(LogAttribute.DELEGATOR_USERNAME.getAttributeName()));
        transactionLogEntity.setUsername(attributes.get(LogAttribute.USERNAME.getAttributeName()));
        transactionLogEntity.setStatusCode(attributes.get(LogAttribute.STATUS_CODE.getAttributeName()));
        transactionLogEntity.setAccountNo(attributes.get(LogAttribute.ACCOUNT_NO.getAttributeName()));
        transactionLogEntity.setServerCode(attributes.get((LogAttribute.PROVIDER_CODE.getAttributeName())));
//        transactionLogEntity.setMessageSequenceId(attributes.get(LogAttribute.MESSAGE_ID.getAttributeName())); //TODO: uncomment this
        transactionLogEntity.setMessageSequenceId(UUID.randomUUID().toString()); //TODO: remove this , just for demo
        transactionLogEntity.setLogTime(getLogTime(attributes, spanModel)); //TODO
        transactionLogEntity.setServerException(attributes.get(LogAttribute.EXCEPTION_CLASS_NAME.getAttributeName()));
        transactionLogEntity.setDescription(attributes.get(LogAttribute.DESCRIPTION.getAttributeName()));
        transactionLogEntity.setPayload(attributes.get(LogAttribute.MESSAGE.getAttributeName()));
        transactionLogEntity.setDocNo(attributes.get(LogAttribute.DOC_NO.getAttributeName()));
        transactionLogEntity.setTerminalType(attributes.get(LogAttribute.TERMINAL_TYPE.getAttributeName()));
        transactionLogEntity.setInterBank(convertToBoolean(attributes, LogAttribute.INTER_BANK));
        transactionLogEntity.setAmount(convertToLong(attributes, LogAttribute.AMOUNT));
        transactionLogEntity.setTerminalId(attributes.get(LogAttribute.TERMINAL_Id.getAttributeName()));
        transactionLogEntity.setCardNo(attributes.get(LogAttribute.CARD_NO.getAttributeName()));
//        transactionLogEntity.setClientDate(new Date(LogAttribute.CLIENT_DATE.getAttributeName())); //TODO
        transactionLogEntity.setExternalSequenceId(attributes.get(LogAttribute.EXTERNAL_SEQUENCE_ID.getAttributeName()));
        transactionLogEntity.setOriginalSequenceId(attributes.get(LogAttribute.ORIGINAL_SEQUENCE_ID.getAttributeName()));
        transactionLogEntity.setDestination(attributes.get(LogAttribute.DESTINATION.getAttributeName()));
        setIpAddress(transactionLogEntity);
        return transactionLogEntity;
    }

    private static Timestamp getLogTime(Map<String, String> attributes, SpanModel spanModel) {
        Timestamp timestamp = null;
        String logTime = attributes.get(LogAttribute.LOG_TIME.getAttributeName());
//        if (StringUtils.isNotBlank(logTime)) {
//            timestamp = new Timestamp(logTime);
//        } else {
//            date = new Timestamp(TimeUnit.NANOSECONDS.toMillis(spanModel.getEndEpochNanos()));
//        }
        //TODO change it log time foramt
        return new Timestamp(System.currentTimeMillis());
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

    private void setIpAddress(TransactionLogEntity transactionLogEntity) {
        transactionLogEntity.setClientIPAddress(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName());
        transactionLogEntity.setClientPhoneNumber(LogAttribute.CLIENT_REMOTE_ADDRESS.getAttributeName());
    }

    private LogMessage deserializeLogMessage(String msg) throws JsonProcessingException {
        return objectMapper.readValue(msg, LogMessage.class);
    }
}
