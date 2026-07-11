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
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class ObservationConfigurationValidator implements SmartInitializingSingleton {
    private static final Pattern FILE_SIZE = Pattern.compile("(?i)^\\d+\\s*(B|KB|MB|GB|TB)?$");
    private static final Set<String> ALLOWED_ENVIRONMENTS = Set.of("dev", "test", "pilot", "prod");
    private static final Set<String> ALLOWED_FORMATS = Set.of("simple", "jsonl");

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
        validateFormats();
        validateScmEnvironment();
        validateConfigLabel();
        validateMetadata();
        validateSharedFileConfiguration();
        validateLogbackAvailability();
        validateLogConfiguration();
        validateTraceConfiguration();
        validateAuditConfiguration();
    }

    private void validateFormats() {
        ObservationProperties.LogProperties log = properties.getLog();
        ObservationProperties.TraceProperties trace = properties.getTrace();
        ObservationProperties.AuditProperties audit = properties.getAudit();

        validateFormat("scm.observation.log.console.format",
                log == null || log.getConsole() == null ? null : log.getConsole().getFormat());
        validateFormat("scm.observation.log.file.format",
                log == null || log.getFile() == null ? null : log.getFile().getFormat());
        validateFormat("scm.observation.trace.console.format",
                trace == null || trace.getConsole() == null ? null : trace.getConsole().getFormat());
        validateFormat("scm.observation.trace.file.format",
                trace == null || trace.getFile() == null ? null : trace.getFile().getFormat());
        validateFormat("scm.observation.audit.console.format",
                audit == null || audit.getConsole() == null ? null : audit.getConsole().getFormat());
        validateFormat("scm.observation.audit.file.format",
                audit == null || audit.getFile() == null ? null : audit.getFile().getFormat());
    }

    private void validateFormat(String property, String value) {
        if (value == null || !ALLOWED_FORMATS.contains(value)) {
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
        if (file.isEnabled()) {
            requireConfigured(prefix + ".directory", file.getDirectory());
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

    private void validateFileSize(String property, String value) {
        if (value != null && !value.isBlank() && !FILE_SIZE.matcher(value.trim()).matches()) {
            throw new IllegalStateException(property + " must be a valid file size such as 100MB or 10GB.");
        }
    }

    private void validateSharedFileConfiguration() {
        if (!fileOutputEnabled()) {
            return;
        }
        String rootDirectory = textOrNull(property("scm.observation.file.root-directory"));
        if (rootDirectory == null) {
            throw new IllegalStateException("scm.observation.file.root-directory must be configured when observation file output is enabled.");
        }
        String baseNamePattern = textOrNull(property("scm.observation.file.base-name-pattern"));
        if (baseNamePattern == null) {
            throw new IllegalStateException("scm.observation.file.base-name-pattern must be configured when observation file output is enabled.");
        }
        if (baseNamePattern.contains("/") || baseNamePattern.contains("\\")) {
            throw new IllegalStateException("scm.observation.file.base-name-pattern must not contain path separators.");
        }
        if (baseNamePattern.contains("%d") || baseNamePattern.contains("%i")) {
            throw new IllegalStateException("scm.observation.file.base-name-pattern must not contain Logback date or roll-index tokens.");
        }
        String lowerCaseBaseName = baseNamePattern.toLowerCase(Locale.ROOT);
        if (lowerCaseBaseName.endsWith(".jsonl")
                || lowerCaseBaseName.endsWith(".log")
                || lowerCaseBaseName.endsWith(".gz")
                || lowerCaseBaseName.endsWith(".zip")) {
            throw new IllegalStateException("scm.observation.file.base-name-pattern must not include a file extension.");
        }
    }

    private boolean fileOutputEnabled() {
        return fileEnabled(properties.getLog() == null ? null : properties.getLog().getFile())
                || fileEnabled(properties.getTrace() == null ? null : properties.getTrace().getFile())
                || fileEnabled(properties.getAudit() == null ? null : properties.getAudit().getFile());
    }

    private boolean fileEnabled(ObservationProperties.FileProperties file) {
        return file != null && file.isEnabled();
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

    private void validateConfigLabel() {
        validateOptionalSingleLabel("SCM_LABEL", property("SCM_LABEL"));
        validateOptionalSingleLabel("spring.cloud.config.label", property("spring.cloud.config.label"));
        validateOptionalSingleLabel("spring.cloud.config.server.git.default-label",
                property("spring.cloud.config.server.git.default-label"));
        String label = firstText(
                property("spring.cloud.config.label"),
                property("spring.cloud.config.server.git.default-label"),
                "master"
        );
        if (label == null || label.isBlank()) {
            throw new IllegalStateException("SCM_LABEL/spring.cloud.config.label must not be blank.");
        }
        if (label.contains(",")) {
            throw new IllegalStateException("SCM_LABEL/spring.cloud.config.label must contain exactly one label.");
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
        String timeZone = textOrNull(property("scm.metadata.time-zone"));
        if (timeZone != null) {
            try {
                ZoneId.of(timeZone);
            } catch (RuntimeException ex) {
                throw new IllegalStateException("scm.metadata.time-zone must be a valid Java ZoneId.", ex);
            }
        }
    }

    private void rejectNonDevPlaceholder(String property, String value) {
        if ("local".equals(value) || "default".equals(value) || value.startsWith("local-")) {
            throw new IllegalStateException(property + " must be nonblank and must not use a local/default placeholder outside dev.");
        }
    }

    private void validateOptionalSingleLabel(String property, String value) {
        if (value == null) {
            return;
        }
        if (value.isBlank()) {
            throw new IllegalStateException(property + " must not be blank when configured.");
        }
        if (value.contains(",")) {
            throw new IllegalStateException(property + " must contain exactly one label.");
        }
    }

    private String property(String key) {
        return environment == null ? null : environment.getProperty(key);
    }

    private String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String firstText(String... candidates) {
        for (String candidate : candidates) {
            String text = textOrNull(candidate);
            if (text != null) {
                return text;
            }
        }
        return null;
    }
}
