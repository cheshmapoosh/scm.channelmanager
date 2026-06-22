package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.ScmCommonLogAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmObservationDocumentAttributes;

import java.time.Instant;
import java.util.Locale;
import java.util.Set;

public class ObservationDocumentFactory {
    private static final Set<String> ALLOWED_CORRELATION_TYPES = Set.of(
            "lifecycle",
            "request",
            "message",
            "job",
            "batch",
            "operation",
            "unknown"
    );

    private final ObservationContext context;
    private final ObservationAttributeRegistry registry;
    private final ObservationSanitizer sanitizer;
    private final ObservationRecordValidator validator;

    public ObservationDocumentFactory(
            ObservationContext context,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer
    ) {
        this.context = context;
        this.registry = registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
        this.sanitizer = sanitizer;
        this.validator = new ObservationRecordValidator(this.registry);
    }

    public ObservationDocumentBuilder builder(
            ObservationStream stream,
            ObservationRecordKind kind,
            boolean errorContext
    ) {
        return new ObservationDocumentBuilder(stream, kind, errorContext, registry, sanitizer, validator);
    }

    public ObservationDocumentBuilder log(
            ObservationRecordKind kind,
            boolean errorContext,
            Instant timestamp,
            String level,
            String loggerName,
            String threadName,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.LOG, kind, errorContext);
        builder.put(ScmCommonLogAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ScmCommonLogAttributes.LOG_LEVEL, textOrDefault(level, "INFO").toUpperCase(Locale.ROOT));
        builder.put(ScmCommonLogAttributes.LOG_LOGGER, textOrDefault(loggerName, "application"));
        builder.put(ScmCommonLogAttributes.PROCESS_THREAD_NAME, textOrDefault(threadName, Thread.currentThread().getName()));
        builder.put(ScmCommonLogAttributes.MESSAGE, textOrDefault(message, ""));
        builder.put(ScmCommonLogAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ScmCommonLogAttributes.CORRELATION_TYPE, correlationType(correlationType, kind));
        return builder;
    }

    public ObservationDocumentBuilder trace(
            ObservationRecordKind kind,
            boolean errorContext,
            Instant timestamp,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.TRACE, kind, errorContext);
        builder.put(ScmObservationDocumentAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ScmObservationDocumentAttributes.MESSAGE, textOrDefault(message, "trace observation"));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_TYPE, correlationType(correlationType, kind));
        return builder;
    }

    public ObservationDocumentBuilder audit(
            ObservationRecordKind kind,
            boolean errorContext,
            Instant timestamp,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.AUDIT, kind, errorContext);
        builder.put(ScmObservationDocumentAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ScmObservationDocumentAttributes.MESSAGE, textOrDefault(message, "audit observation"));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_TYPE, correlationType(correlationType, kind));
        return builder;
    }

    public void putRuntimeContext(ObservationDocumentBuilder builder) {
        if (builder == null) {
            return;
        }
        builder.put(ScmCommonLogAttributes.DEPLOYMENT_SERVICE_NAME, textOrDefault(context == null ? null : context.appName(), "application"));
        builder.put(ScmCommonLogAttributes.DEPLOYMENT_SERVICE_VERSION, "unknown");
        builder.put(ScmCommonLogAttributes.DEPLOYMENT_ENVIRONMENT, textOrDefault(context == null ? null : context.appProfile(), "default"));
        builder.put(ScmCommonLogAttributes.SCM_RUNTIME, "unknown");
    }

    public String correlationType(String requestedValue, ObservationRecordKind kind) {
        String normalized = requestedValue == null ? null : requestedValue.trim().toLowerCase(Locale.ROOT);
        if (normalized != null && ALLOWED_CORRELATION_TYPES.contains(normalized)) {
            return normalized;
        }
        return kind == ObservationRecordKind.CONTEXT ? "lifecycle" : "unknown";
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
