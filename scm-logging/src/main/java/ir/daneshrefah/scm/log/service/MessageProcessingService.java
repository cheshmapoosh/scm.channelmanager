package ir.daneshrefah.scm.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.log.model.LogMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProcessingService {

    private final List<ConverterService> converters;
    private final ObjectMapper objectMapper;

    public void processMessage(String rawMessage) throws Exception {
        LogMessage logMessage = deserializeLogMessage(rawMessage);
        for (ConverterService converter : converters) {
            try {
                if (converter.supports(logMessage)) {
                    converter.convertAndPersist(logMessage);
                }
            } catch (Exception e) {
                log.error("Failed to process message in converter {}: {}", converter.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }

    private LogMessage deserializeLogMessage(String msg) throws Exception {
        return objectMapper.readValue(msg, LogMessage.class);
    }
}
