package ir.daneshrefah.scm.logging.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.model.LogMessage;
import org.springframework.stereotype.Service;

@Service
public interface ConverterService {
    LogMessage convertToLogMessage(String msg) throws JsonProcessingException;
    LogTraceEntity convertToLogTraceEntity(LogMessage logMessage);
}
