package ir.daneshrefah.scm.logging.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-04
 */
@Component
//@ConditionalOnProperty(name = "scm.log.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnProperty(name = "scm.log.type", havingValue = "file", matchIfMissing = true)
public class FileEventProducer extends EventProducer {

    @Value("${scm.log.file:events.log}")
    private String logFileName;

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProducer.class);

    private final ObjectMapper objectMapper;

    public FileEventProducer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendEvent(Event event) {
        String eventData = serializeEvent(event);
        LOGGER.info(eventData);
    }

    private String serializeEvent(Event event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

}
