
package ir.daneshrefah.scm.log.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageProcessingService {
    private final List<ConverterService> converters;

    public void processMessage(String rawMessage) {
        for (ConverterService converter : converters) {
            try {
                converter.convertAndPersist(rawMessage);
            } catch (Exception e) {
                log.error("Failed to process message in converter {}: {}", converter.getClass().getSimpleName(), e.getMessage(), e);
            }
        }
    }
}
