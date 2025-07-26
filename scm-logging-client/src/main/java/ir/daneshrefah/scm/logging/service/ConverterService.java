package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.common.data.entity.logging.LogTraceEntity;
import org.springframework.core.log.LogMessage;
import org.springframework.stereotype.Service;

@Service
public interface ConverterService {
    LogMessage convertToLogMessage(String msg) throws JsonProcessingException;
    LogTraceEntity convertToLogTraceEntity(LogMessage logMessage);
}
