package ir.daneshrefah.scm.logging.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class LoggerConfig {

    private final String logFileName;
    private final String fileDirectory;
    private final String logPattern;
    private final String filePatternName;
    private final String fileSize;
    private final String rollingArchiveDirectory;
    private final int keepLogHistory;

    public LoggerConfig(@Value("${scm.log.trace.file-name}") String logFileName,
                        @Value("${scm.log.trace.file-directory}") String fileDirectory,
                        @Value("${scm.log.trace.log-pattern}") String logPattern,
                        @Value("${scm.log.trace.file-name-pattern}") String filePatternName,
                        @Value("${scm.log.trace.file-size}") String fileSize,
                        @Value("${scm.log.trace.rolling-archive-directory}") String rollingArchiveDirectory,
                        @Value("${scm.log.trace.keep-log-history}") int keepLogHistory) {
        this.logFileName = logFileName;
        this.fileDirectory = fileDirectory;
        this.logPattern = logPattern;
        this.filePatternName = filePatternName;
        this.fileSize = fileSize;
        this.keepLogHistory = keepLogHistory;
        this.rollingArchiveDirectory = rollingArchiveDirectory;
    }

    @Bean
    @ConditionalOnMissingBean(value = ObjectMapper.class)
    public ObjectMapper init() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }

    @Bean
    public Logger logger() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Create and configure the rolling file appender
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(context);
        rollingFileAppender.setName("FileAppender");
        rollingFileAppender.setFile(fileDirectory + File.separator + logFileName);

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
        rollingPolicy.setFileNamePattern(fileDirectory + File.separator + rollingArchiveDirectory + File.separator + filePatternName);
        rollingPolicy.setMaxFileSize(FileSize.valueOf(fileSize)); // Max size of each log file
        rollingPolicy.setMaxHistory(keepLogHistory); // Keep up to ? days of log files
        rollingPolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.start();

        // Create and configure the AsyncAppender
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setQueueSize(512);
        asyncAppender.setContext(context);
        asyncAppender.addAppender(rollingFileAppender);
        asyncAppender.start();

        // Get the logger and attach the async appender
        Logger logger = LoggerFactory.getLogger(LoggerConfig.class);
        ((ch.qos.logback.classic.Logger) logger).setLevel(Level.TRACE);
        ((ch.qos.logback.classic.Logger) logger).detachAndStopAllAppenders();
        ((ch.qos.logback.classic.Logger) logger).addAppender(asyncAppender);
        ((ch.qos.logback.classic.Logger) logger).setAdditive(false);
        return logger;
    }

    @PreDestroy
    public void shutdown() {
        ((ch.qos.logback.classic.Logger) logger()).detachAndStopAllAppenders();
    }
}