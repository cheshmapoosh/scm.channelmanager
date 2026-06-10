package ir.daneshrefah.scm.observation.autoconfigure;

import ir.daneshrefah.scm.observation.ObservationProperties;
import ir.daneshrefah.scm.observation.logback.LogbackObservationEventPublisher;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.policy.ObservationSignalPolicy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;

import java.nio.file.Path;
import java.util.regex.Pattern;

public class ObservationConfigurationValidator implements SmartInitializingSingleton {
    private static final Pattern FILE_SIZE = Pattern.compile("(?i)^\\d+\\s*(B|KB|MB|GB|TB)?$");

    private final ObservationProperties properties;
    private final ObservationSignalPolicy signalPolicy;
    private final ObjectProvider<LogbackObservationEventPublisher> publisherProvider;

    public ObservationConfigurationValidator(
            ObservationProperties properties,
            ObservationSignalPolicy signalPolicy,
            ObjectProvider<LogbackObservationEventPublisher> publisherProvider
    ) {
        this.properties = properties == null ? new ObservationProperties() : properties;
        this.signalPolicy = signalPolicy;
        this.publisherProvider = publisherProvider;
    }

    @Override
    public void afterSingletonsInstantiated() {
        validateLogbackAvailability();
        validateLogConfiguration();
        validateTraceConfiguration();
        validateAuditConfiguration();
    }

    private void validateLogbackAvailability() {
        if (!traceOrAuditEnabled()) {
            return;
        }
        if (!"ch.qos.logback.classic.LoggerContext".equals(LoggerFactory.getILoggerFactory().getClass().getName())) {
            throw new IllegalStateException("SCM observation TRACE/AUDIT is enabled but Logback is not the active logging backend.");
        }
        if (publisherProvider == null || publisherProvider.getIfAvailable() == null) {
            throw new IllegalStateException("SCM observation TRACE/AUDIT is enabled but the Logback event publisher is unavailable.");
        }
    }

    private void validateLogConfiguration() {
        ObservationProperties.LogProperties log = properties.getLog();
        if (log == null) {
            return;
        }
        validateFile("scm.observation.log.file", log.getFile());
        validateRolling("scm.observation.log.rolling", log.getRolling());
    }

    private void validateTraceConfiguration() {
        ObservationProperties.TraceProperties trace = properties.getTrace();
        if (trace == null) {
            return;
        }
        validateFile("scm.observation.trace.file", trace.getFile());
        validateRolling("scm.observation.trace.rolling", trace.getRolling());
        validateAsync("scm.observation.trace.async", trace.getAsync());
    }

    private void validateAuditConfiguration() {
        ObservationProperties.AuditProperties audit = properties.getAudit();
        if (audit == null) {
            return;
        }
        validateFile("scm.observation.audit.file", audit.getFile());
        validateRolling("scm.observation.audit.rolling", audit.getRolling());
        validateAsync("scm.observation.audit.async", audit.getAsync());
    }

    private void validateFile(String prefix, ObservationProperties.FileProperties file) {
        if (file == null) {
            return;
        }
        validateDirectory(prefix + ".directory", file.getDirectory());
        validateDirectory(prefix + ".archive-directory", file.getArchiveDirectory());
        if (file.isEnabled()) {
            requireConfigured(prefix + ".directory", file.getDirectory());
            requireText(prefix + ".file-name", file.getFileName());
            requireConfigured(prefix + ".archive-directory", file.getArchiveDirectory());
        }
    }

    private void validateRolling(String prefix, ObservationProperties.RollingProperties rolling) {
        if (rolling == null) {
            return;
        }
        validateFileSize(prefix + ".max-file-size", rolling.getMaxFileSize());
        validateFileSize(prefix + ".total-size-cap", rolling.getTotalSizeCap());
        Integer maxHistory = rolling.getMaxHistory();
        if (maxHistory != null && maxHistory < 1) {
            throw new IllegalStateException(prefix + ".max-history must be greater than zero.");
        }
    }

    private void validateAsync(String prefix, ObservationProperties.AsyncProperties async) {
        if (async == null) {
            return;
        }
        if (async.isEnabled()) {
            Integer queueSize = async.getQueueSize();
            if (queueSize != null && queueSize < 1) {
                throw new IllegalStateException(prefix + ".queue-size must be greater than zero.");
            }
        }
        Integer discardingThreshold = async.getDiscardingThreshold();
        if (discardingThreshold != null && discardingThreshold < 0) {
            throw new IllegalStateException(prefix + ".discarding-threshold must not be negative.");
        }
        Integer maxFlushTime = async.getMaxFlushTime();
        if (maxFlushTime != null && maxFlushTime < 0) {
            throw new IllegalStateException(prefix + ".max-flush-time must not be negative.");
        }
    }

    private void validateDirectory(String property, Path directory) {
        if (directory != null && directory.toString().isBlank()) {
            throw new IllegalStateException(property + " must not be blank when configured.");
        }
    }

    private void requireConfigured(String property, Path directory) {
        if (directory == null || directory.toString().isBlank()) {
            throw new IllegalStateException(property + " must be configured when file output is enabled.");
        }
    }

    private void requireText(String property, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(property + " must be configured when file output is enabled.");
        }
    }

    private void validateFileSize(String property, String value) {
        if (value != null && !value.isBlank() && !FILE_SIZE.matcher(value.trim()).matches()) {
            throw new IllegalStateException(property + " must be a valid file size such as 100MB or 10GB.");
        }
    }

    private boolean traceOrAuditEnabled() {
        return signalPolicy != null
                && (signalPolicy.isEnabled(ObservationSignal.TRACE)
                || signalPolicy.isEnabled(ObservationSignal.AUDIT));
    }
}
