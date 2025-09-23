package ir.daneshrefah.scm.log.config;

import ch.qos.logback.classic.pattern.LoggerConverter;
import ir.daneshrefah.scm.log.service.ConverterService;
import jakarta.transaction.NotSupportedException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConverterConfiguration {

    @Bean
    @ConditionalOnMissingBean(LoggerConverter.class)
    public ConverterService disabledLoggerConverter() {
        return logMessage -> {
            throw new NotSupportedException("DisabledConverterServiceImpl");
        };
    }
}
