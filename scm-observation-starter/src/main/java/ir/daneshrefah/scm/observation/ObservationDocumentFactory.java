package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.ScmCommonLogAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmObservationDocumentAttributes;

import java.time.Instant;
import java.util.Locale;

public class ObservationDocumentFactory {
    private final ObservationContext context;
    private final ObservationAttributeRegistry registry;
    private final ObservationSanitizer sanitizer;

    public ObservationDocumentFactory(
            ObservationContext context,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer
    ) {
        this.context = context;
        this.registry = registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
        this.sanitizer = sanitizer;
    }

    public ObservationDocumentBuilder builder(
            ObservationStream stream
    ) {
        return new ObservationDocumentBuilder(stream, registry, sanitizer);
    }

    public ObservationDocumentBuilder log(
            Instant timestamp,
            String level,
            String loggerName,
            String threadName,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.LOG);
        builder.put(ScmCommonLogAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ScmCommonLogAttributes.LOG_LEVEL, textOrDefault(level, "INFO").toUpperCase(Locale.ROOT));
        builder.put(ScmCommonLogAttributes.LOG_LOGGER, textOrDefault(loggerName, "application"));
        builder.put(ScmCommonLogAttributes.PROCESS_THREAD_NAME, textOrDefault(threadName, Thread.currentThread().getName()));
        builder.put(ScmCommonLogAttributes.MESSAGE, textOrDefault(message, ""));
        builder.put(ScmCommonLogAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ScmCommonLogAttributes.CORRELATION_TYPE, correlationType(correlationType));
        return builder;
    }

    public ObservationDocumentBuilder trace(
            Instant timestamp,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.TRACE);
        builder.put(ScmObservationDocumentAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ScmObservationDocumentAttributes.MESSAGE, textOrDefault(message, "trace observation"));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_TYPE, correlationType(correlationType));
        return builder;
    }

    public ObservationDocumentBuilder audit(
            Instant timestamp,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.AUDIT);
        builder.put(ScmObservationDocumentAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ScmObservationDocumentAttributes.MESSAGE, textOrDefault(message, "audit observation"));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ScmObservationDocumentAttributes.CORRELATION_TYPE, correlationType(correlationType));
        return builder;
    }

    public void putRuntimeContext(ObservationDocumentBuilder builder) {
        if (builder == null) {
            return;
        }
        if (context == null) {
            return;
        }
        builder.put(ScmCommonLogAttributes.DEPLOYMENT_SERVICE_NAME, context.appName());
        builder.put(ScmCommonLogAttributes.DEPLOYMENT_SERVICE_VERSION, context.serviceVersion());
        builder.put(ScmCommonLogAttributes.DEPLOYMENT_ENVIRONMENT, context.appProfile());
        builder.put(ScmCommonLogAttributes.SCM_RUNTIME, context.runtime());
    }

    public String correlationType(String requestedValue) {
        return requestedValue == null || requestedValue.isBlank()
                ? CorrelationType.UNKNOWN.value()
                : requestedValue.trim().toLowerCase(Locale.ROOT);
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
