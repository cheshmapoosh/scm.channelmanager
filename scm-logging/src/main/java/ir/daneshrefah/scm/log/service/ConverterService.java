package ir.daneshrefah.scm.log.service;

import ir.daneshrefah.scm.log.model.LogMessage;
import org.springframework.stereotype.Service;

@Service
public interface ConverterService {
    boolean supports(LogMessage logMessage);
    void convertAndPersist(LogMessage logMessage) throws Exception;
}
