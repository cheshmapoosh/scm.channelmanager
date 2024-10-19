package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.logging.entity.TransactionLogEntity;
import ir.daneshrefah.scm.logging.model.LogMessage;
import org.springframework.stereotype.Service;

@Service
public interface ConverterServiceImpl {
    LogMessage convertToLogMessage(String msg) throws JsonProcessingException;
    TransactionLogEntity convertToTransactionLogEntity(LogMessage logMessage);
}
