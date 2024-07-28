package ir.daneshrefah.scm.logging.impl;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.core.FileAppender;
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
import org.springframework.beans.factory.annotation.Value;
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

    private final Logger LOGGER;

    private final ObjectMapper objectMapper;
    BlockingQueue<Event> loggingQueue = new LinkedBlockingQueue<>();
//    ConcurrentLinkedQueue<Event> loggingQueue = new ConcurrentLinkedQueue<>();


    public FileEventProducer(@Value("${scm.log.file-name}") String logFileName) {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule module = new SimpleModule();
        module.addSerializer(HttpServletRequest.class, HttpServletRequestSerializer.INSTANT);
        module.addSerializer(Exchange.class, ExchangeSerializer.INSTANT);
        objectMapper.registerModule(module);
        initLogThread();
        LOGGER = initLogger(logFileName);
    }

    private Logger initLogger(String logFileName) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Create and configure the file appender
        FileAppender fileAppender = new FileAppender();
        fileAppender.setContext(context);
        fileAppender.setName("FileEventProducerFileAppender");
        fileAppender.setFile(logFileName);


        // Create and configure the encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %msg%n");
        encoder.start();

        fileAppender.setEncoder(encoder);
        fileAppender.start();

        // Create and configure the AsyncAppender
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.addAppender(fileAppender);
        asyncAppender.start();

        // Get the logger and attach the async appender
        Logger logger = (Logger) LoggerFactory.getLogger(FileEventProducer.class);
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.ALL);
        ((ch.qos.logback.classic.Logger) logger).detachAndStopAllAppenders();
        ((ch.qos.logback.classic.Logger) logger).addAppender(asyncAppender);
        ((ch.qos.logback.classic.Logger) logger).setAdditive(false);

        return logger;
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
                    LOGGER.error("error unknown." + ((null != event) ? event.getCorrelationId() : "null"), e);
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
