package ir.daneshrefah.scm.logging.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProducer.class);

    private final ObjectMapper objectMapper;
    BlockingQueue<Event> loggingQueue = new LinkedBlockingQueue<>();


    public FileEventProducer() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        initLogThread();
    }

    private void initLogThread() {
        Thread loggingThread = new Thread(() -> {
            while (true) {
                Event event = null;
                try {
                    event = loggingQueue.take();
                } catch (InterruptedException e) {
                    LOGGER.error(e.getMessage(), e);
                }
                try {
                    String json = objectMapper.writeValueAsString(event);
                    LOGGER.info(json);
                } catch (JsonProcessingException e) {
                    LOGGER.error("error serialize event:" + event.getCorrelationId(), e);
                }
            }
        });
        loggingThread.start();
    }

    @Override
    public void sendEvent(Event event) {
        loggingQueue.add(event);
//        String eventData = serializeEvent(event);
//        LOGGER.info(eventData);
    }

}
