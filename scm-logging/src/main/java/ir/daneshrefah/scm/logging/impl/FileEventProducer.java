package ir.daneshrefah.scm.logging.impl;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ir.daneshrefah.scm.common.model.event.Event;
import ir.daneshrefah.scm.logging.api.EventProducer;
import ir.daneshrefah.scm.logging.serializer.ExchangeSerializer;
import ir.daneshrefah.scm.logging.serializer.HttpServletRequestSerializer;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
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


    public FileEventProducer(@Value("${scm.log.file-name}") String logFileName,
                             @Value("${scm.log.file-directory}") String fileDirectory,
                             @Value("${scm.log.log-pattern}") String logPattern,
                             @Value("${scm.log.file-name-pattern}") String fileNamePattern,
                             @Value("${scm.log.file-size}") String fileSize,
                             @Value("${scm.log.keep-log-history}") int keepLogHistory
    ) {
        this.objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        SimpleModule module = new SimpleModule();
        module.addSerializer(HttpServletRequest.class, HttpServletRequestSerializer.INSTANT);
        module.addSerializer(Exchange.class, ExchangeSerializer.INSTANT);
        objectMapper.registerModule(module);
        initLogThread();
        LOGGER = initLogger(logFileName, fileDirectory,logPattern,fileNamePattern,fileSize,keepLogHistory);
    }

    private Logger initLogger(String logFileName,
                              String fileDirectory,
                              String logPattern,
                              String fileNamePattern,
                              String fileSize,
                              int keepLogHistory) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Create and configure the rolling file appender
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(context);
        rollingFileAppender.setName("FileEventProducerFileAppender");
        rollingFileAppender.setFile(logFileName);

        // Create and configure the encoder
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(logPattern);
        encoder.start();

        rollingFileAppender.setEncoder(encoder);

        // Create and configure the rolling policy
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(fileDirectory + File.separator + logFileName + fileNamePattern); // Filename pattern
        rollingPolicy.setMaxFileSize(FileSize.valueOf(fileSize)); // Max size of each log file
        rollingPolicy.setMaxHistory(keepLogHistory); // Keep up to 30 days of log files
        rollingPolicy.start();


        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.start();

        // Create and configure the AsyncAppender
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(context);
        asyncAppender.addAppender(rollingFileAppender);
        asyncAppender.start();

        // Get the logger and attach the async appender
        Logger logger = LoggerFactory.getLogger(FileEventProducer.class);
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
