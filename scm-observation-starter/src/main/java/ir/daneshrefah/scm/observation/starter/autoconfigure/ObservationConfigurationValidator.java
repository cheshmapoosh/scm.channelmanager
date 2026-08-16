package ir.daneshrefah.scm.observation.starter.autoconfigure;

import ir.daneshrefah.scm.observation.starter.ObservationProperties;
import ir.daneshrefah.scm.observation.starter.logback.LogbackObservationEventPublisher;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignalPolicy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Set;
import java.util.regex.Pattern;

public class ObservationConfigurationValidator implements SmartInitializingSingleton {
    private static final Pattern FILE_SIZE = Pattern.compile("(?i)^\\d+\\s*(B|KB|MB|GB|TB)?$");
    private static final Set<String> ALLOWED_ENVIRONMENTS = Set.of("dev", "test", "pilot", "prod");
    private static final Set<String> ALLOWED_CONSOLE_FORMATS = Set.of("simple", "jsonl");

    private final ObservationProperties properties;
    private final ObservationSignalPolicy signalPolicy;
    private final ObjectProvider<LogbackObservationEventPublisher> publisherProvider;
    private final Environment environment;

    public ObservationConfigurationValidator(
            ObservationProperties properties,
            ObservationSignalPolicy signalPolicy,
            ObjectProvider<LogbackObservationEventPublisher> publisherProvider,
            Environment environment
    ) {
        this.properties = properties == null ? new ObservationProperties() : properties;
        this.signalPolicy = signalPolicy;
        this.publisherProvider = publisherProvider;
        this.environment = environment;
    }

    @Override
    public void afterSingletonsInstantiated() {
        validateConsoleFormats();
        validateTimeZone();
        if (fileOutputEnabled()) {
            validateScmEnvironment();
            validateMetadata();
            validateSharedFileConfiguration();
        }
        validateLogbackAvailability();
        validateLogConfiguration();
        validateTraceConfiguration();
        validateAuditConfiguration();
    }

    private void validateConsoleFormats() {
        ObservationProperties.LogProperties log = properties.getLog();
        ObservationProperties.TraceProperties trace = properties.getTrace();
        ObservationProperties.AuditProperties audit = properties.getAudit();
        validateConsoleFormat("scm.observation.log.console.format",
                log == null || log.getConsole() == null ? null : log.getConsole().getFormat());
        validateConsoleFormat("scm.observation.trace.console.format",
                trace == null || trace.getConsole() == null ? null : trace.getConsole().getFormat());
        validateConsoleFormat("scm.observation.audit.console.format",
                audit == null || audit.getConsole() == null ? null : audit.getConsole().getFormat());
    }

    private void validateConsoleFormat(String property, String value) {
        if (value == null || !ALLOWED_CONSOLE_FORMATS.contains(value)) {
            throw new IllegalStateException(property + " must be one of: simple, jsonl.");
        }
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
        if (log != null) {
            validateRolling("scm.observation.log.rolling", log.getRolling());
        }
    }

    private void validateTraceConfiguration() {
        ObservationProperties.TraceProperties trace = properties.getTrace();
        if (trace != null) {
            validateRolling("scm.observation.trace.rolling", trace.getRolling());
            validateAsync("scm.observation.trace.async", trace.getAsync());
        }
    }

    private void validateAuditConfiguration() {
        ObservationProperties.AuditProperties audit = properties.getAudit();
        if (audit != null) {
            validateRolling("scm.observation.audit.rolling", audit.getRolling());
            validateAsync("scm.observation.audit.async", audit.getAsync());
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

    private void validateFileSize(String property, String value) {
        if (value != null && !value.isBlank() && !FILE_SIZE.matcher(value.trim()).matches()) {
            throw new IllegalStateException(property + " must be a valid file size such as 100MB or 10GB.");
        }
    }

    private void validateSharedFileConfiguration() {
        ObservationProperties.FileStorageProperties file = properties.getFile();
        Path rootDirectory = file == null ? null : file.getRootDirectory();
        if (rootDirectory == null || rootDirectory.toString().isBlank()) {
            throw new IllegalStateException(
                    "scm.observation.file.root-directory must be configured when observation file output is enabled."
            );
        }
        String archiveDirectoryName = file == null ? null : textOrNull(file.getArchiveDirectoryName());
        if (archiveDirectoryName == null
                || ".".equals(archiveDirectoryName)
                || "..".equals(archiveDirectoryName)
                || Path.of(archiveDirectoryName).isAbsolute()
                || archiveDirectoryName.contains("/")
                || archiveDirectoryName.contains("\\")) {
            throw new IllegalStateException(
                    "scm.observation.file.archive-directory-name must be a nonblank directory name without path separators."
            );
        }
    }

    private boolean fileOutputEnabled() {
        return fileSinkEnabled(ObservationSignal.LOG, properties.getLog() == null ? null : properties.getLog().getFile())
                || fileSinkEnabled(ObservationSignal.TRACE, properties.getTrace() == null ? null : properties.getTrace().getFile())
                || fileSinkEnabled(ObservationSignal.AUDIT, properties.getAudit() == null ? null : properties.getAudit().getFile());
    }

    private boolean fileSinkEnabled(ObservationSignal signal, ObservationProperties.FileSinkProperties file) {
        return file != null && file.isEnabled() && signalPolicy != null && signalPolicy.isEnabled(signal);
    }

    private boolean traceOrAuditEnabled() {
        return signalPolicy != null
                && (signalPolicy.isEnabled(ObservationSignal.TRACE)
                || signalPolicy.isEnabled(ObservationSignal.AUDIT));
    }

    private void validateScmEnvironment() {
        String rawScmEnv = property("SCM_ENV");
        if (rawScmEnv != null && rawScmEnv.isBlank()) {
            throw new IllegalStateException("SCM_ENV must not be blank.");
        }
        if (rawScmEnv != null && rawScmEnv.contains(",")) {
            throw new IllegalStateException("SCM_ENV must contain exactly one environment value.");
        }
        String configuredScmEnv = textOrNull(rawScmEnv);
        if (configuredScmEnv != null && !ALLOWED_ENVIRONMENTS.contains(configuredScmEnv)) {
            throw new IllegalStateException("SCM_ENV must be one of dev, test, pilot, prod.");
        }

        String springProfilesActive = property("spring.profiles.active");
        if (springProfilesActive != null && springProfilesActive.isBlank()) {
            throw new IllegalStateException("spring.profiles.active/SCM_ENV must not be blank.");
        }
        if (springProfilesActive != null && springProfilesActive.contains(",")) {
            throw new IllegalStateException("spring.profiles.active/SCM_ENV must contain exactly one active profile.");
        }

        String[] activeProfiles = environment == null ? new String[0] : environment.getActiveProfiles();
        if (activeProfiles.length != 1) {
            throw new IllegalStateException("spring.profiles.active/SCM_ENV must resolve to exactly one active SCM profile.");
        }
        String activeProfile = activeProfiles[0];
        if (!ALLOWED_ENVIRONMENTS.contains(activeProfile)) {
            throw new IllegalStateException("spring.profiles.active/SCM_ENV must be one of dev, test, pilot, prod.");
        }
        if (configuredScmEnv != null && !configuredScmEnv.equals(activeProfile)) {
            throw new IllegalStateException("spring.profiles.active must match SCM_ENV.");
        }
    }

    private void validateMetadata() {
        String activeProfile = environment == null || environment.getActiveProfiles().length == 0
                ? "dev"
                : environment.getActiveProfiles()[0];
        String namespace = textOrNull(property("scm.metadata.namespace"));
        if (namespace == null) {
            throw new IllegalStateException("scm.metadata.namespace must be configured.");
        }
        String instanceId = textOrNull(property("scm.metadata.instance-id"));
        if (instanceId == null) {
            throw new IllegalStateException("scm.metadata.instance-id must be configured.");
        }
        if (!"dev".equals(activeProfile)) {
            rejectNonDevPlaceholder("scm.metadata.namespace", namespace);
            rejectNonDevPlaceholder("scm.metadata.instance-id", instanceId);
        }
    }

    private void validateTimeZone() {
        String timeZone = textOrNull(property("scm.metadata.time-zone"));
        if (timeZone == null) {
            return;
        }
        try {
            ZoneId.of(timeZone);
        } catch (RuntimeException ex) {
            throw new IllegalStateException("scm.metadata.time-zone must be a valid Java ZoneId.", ex);
        }
    }

    private void rejectNonDevPlaceholder(String property, String value) {
        if ("local".equals(value) || "default".equals(value) || value.startsWith("local-")) {
            throw new IllegalStateException(property + " must be nonblank and must not use a local/default placeholder outside dev.");
        }
    }

    private String property(String key) {
        return environment == null ? null : environment.getProperty(key);
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
