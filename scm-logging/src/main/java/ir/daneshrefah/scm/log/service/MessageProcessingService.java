package ir.daneshrefah.scm.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.log.model.LogMessage;
import ir.daneshrefah.scm.log.model.SpanModel;
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
                    SpanModel payload = logMessage.getPayload();
                    String traceId = payload != null ? payload.getTraceId() : "null";
                    String spanId = payload != null ? payload.getSpanId() : "null";
                    log.info("Successfully converted and persist log message with {}. traceId: {}, spanId: {}",
                            converter.getClass().getSimpleName(),
                            traceId,
                            spanId);
                }
            } catch (Exception e) {
                logConverterFailure(converter, rawMessage, e);
            }
        }
    }

    private LogMessage deserializeLogMessage(String msg) throws Exception {
        return objectMapper.readValue(msg, LogMessage.class);
    }

    private void logConverterFailure(ConverterService converter, String rawMessage, Exception exception) {
        log.error("Failed to process message in converter {}: {} , message: {}",
                converter.getClass().getSimpleName(),
                exception.getMessage(),
                rawMessage,
                exception);
    }
}
