package ir.daneshrefah.scm.logging.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.domain.event.Event;
import ir.daneshrefah.scm.logging.serializer.ExchangeSerializer;
import ir.daneshrefah.scm.logging.serializer.HttpServletRequestSerializer;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;
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
//    ConcurrentLinkedQueue<Event> loggingQueue = new ConcurrentLinkedQueue<>();


    public FileEventProducer() {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule module = new SimpleModule();
        module.addSerializer(HttpServletRequest.class, HttpServletRequestSerializer.INSTANT);
        module.addSerializer(Exchange.class, ExchangeSerializer.INSTANT);
        objectMapper.registerModule(module);
        initLogThread();
    }

    private void initLogThread() {
        Thread loggingThread = new Thread(() -> {
            while (true) {
                Event event = null;
                try {
                    event = loggingQueue.take();
                    String json = objectMapper.writeValueAsString(event);
                    LOGGER.info(json);
                } catch (InterruptedException e) {
                    LOGGER.error("error read event.", e);
                } catch (JsonProcessingException e) {
                    LOGGER.error("error serialize event:" + event.getCorrelationId(), e);
                } catch (Exception e) {
                    LOGGER.error("error unknown." + ((null != event) ? event.getCorrelationId(): "null"), e);
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
