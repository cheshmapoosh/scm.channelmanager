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
    private final int keepLogHistory;

    public LoggerConfig(@Value("${scm.log.trace.file-name}") String logFileName,
                        @Value("${scm.log.trace.file-directory}") String fileDirectory,
                        @Value("${scm.log.trace.log-pattern}") String logPattern,
                        @Value("${scm.log.trace.file-name-pattern}") String filePatternName,
                        @Value("${scm.log.trace.file-size}") String fileSize,
                        @Value("${scm.log.trace.keep-log-history}") int keepLogHistory) {
        this.logFileName = logFileName;
        this.fileDirectory = fileDirectory;
        this.logPattern = logPattern;
        this.filePatternName = filePatternName;
        this.fileSize = fileSize;
        this.keepLogHistory = keepLogHistory;
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
        File activeLogFile = new File(fileDirectory, logFileName);
        File rollingLogFilePattern = new File(activeLogFile.getParentFile(), new File(filePatternName).getName());

        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(context);
        rollingFileAppender.setName("FileAppender");
        rollingFileAppender.setFile(activeLogFile.getPath());
        rollingFileAppender.setAppend(true);
        rollingFileAppender.setPrudent(false);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(logPattern);
        encoder.start();

        rollingFileAppender.setEncoder(encoder);

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(rollingLogFilePattern.getPath());
        rollingPolicy.setMaxFileSize(FileSize.valueOf(fileSize));
        rollingPolicy.setMaxHistory(keepLogHistory);
        rollingPolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.start();

        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setQueueSize(512);
        asyncAppender.setContext(context);
        asyncAppender.addAppender(rollingFileAppender);
        asyncAppender.start();

        ch.qos.logback.classic.Logger logger = context.getLogger(LoggerConfig.class.getName());
        logger.setLevel(Level.TRACE);
        logger.detachAndStopAllAppenders();
        logger.addAppender(asyncAppender);
        logger.setAdditive(false);
        return logger;
    }

    @PreDestroy
    public void shutdown() {
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(LoggerConfig.class)).detachAndStopAllAppenders();
    }
}
