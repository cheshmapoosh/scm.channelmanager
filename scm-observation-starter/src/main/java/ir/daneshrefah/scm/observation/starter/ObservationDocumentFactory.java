package ir.daneshrefah.scm.observation.starter;

import ir.daneshrefah.scm.observation.starter.attributes.audit.ServiceExecuteAuditAttributes;
import ir.daneshrefah.scm.observation.starter.attributes.log.CommonLogAttributes;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;

import java.time.Instant;
import java.util.Locale;

public class ObservationDocumentFactory {
    private final ObservationContext context;
    private final ObsTargetIndexResolver targetIndexResolver;
    private final ObservationAttributeRegistry registry;
    private final ObservationSanitizer sanitizer;

    public ObservationDocumentFactory(
            ObservationContext context,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer
    ) {
        this(context, registry, sanitizer, new ObsTargetIndexResolver());
    }

    public ObservationDocumentFactory(
            ObservationContext context,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer,
            ObsTargetIndexResolver targetIndexResolver
    ) {
        this.context = context;
        this.targetIndexResolver = targetIndexResolver == null ? new ObsTargetIndexResolver() : targetIndexResolver;
        this.registry = registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
        this.sanitizer = sanitizer;
    }

    public ObservationDocumentBuilder builder(
            ObservationStream stream
    ) {
        return new ObservationDocumentBuilder(stream, context, targetIndexResolver, registry, sanitizer);
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
        builder.put(CommonLogAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(CommonLogAttributes.LOG_LEVEL, textOrDefault(level, "INFO").toUpperCase(Locale.ROOT));
        builder.put(CommonLogAttributes.LOG_LOGGER, textOrDefault(loggerName, "application"));
        builder.put(CommonLogAttributes.PROCESS_THREAD_NAME, textOrDefault(threadName, Thread.currentThread().getName()));
        builder.put(CommonLogAttributes.MESSAGE, textOrDefault(message, ""));
        builder.put(CommonLogAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(CommonLogAttributes.CORRELATION_TYPE, correlationType(correlationType));
        return builder;
    }

    public ObservationDocumentBuilder trace(
            Instant timestamp,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.TRACE);
        builder.put(CommonTraceAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(CommonTraceAttributes.MESSAGE, textOrDefault(message, "trace observation"));
        builder.put(CommonTraceAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(CommonTraceAttributes.CORRELATION_TYPE, correlationType(correlationType));
        putTraceRuntimeContext(builder);
        return builder;
    }

    public ObservationDocumentBuilder audit(
            Instant timestamp,
            String message,
            String correlationId,
            String correlationType
    ) {
        ObservationDocumentBuilder builder = builder(ObservationStream.AUDIT);
        builder.put(ServiceExecuteAuditAttributes.TIMESTAMP, timestamp == null ? Instant.now().toString() : timestamp.toString());
        builder.put(ServiceExecuteAuditAttributes.MESSAGE, textOrDefault(message, "audit observation"));
        builder.put(ServiceExecuteAuditAttributes.CORRELATION_ID, textOrDefault(correlationId, ObservationIds.correlationId()));
        builder.put(ServiceExecuteAuditAttributes.CORRELATION_TYPE, correlationType(correlationType));
        putAuditRuntimeContext(builder);
        return builder;
    }

    public void putRuntimeContext(ObservationDocumentBuilder builder) {
        if (builder == null) {
            return;
        }
        if (context == null) {
            return;
        }
        builder.put(CommonLogAttributes.DEPLOYMENT_SERVICE_NAME, context.appName());
        builder.put(CommonLogAttributes.DEPLOYMENT_SERVICE_VERSION, context.serviceVersion());
        builder.put(CommonLogAttributes.DEPLOYMENT_ENVIRONMENT, context.appProfile());
        builder.put(CommonLogAttributes.SERVICE_NAME, context.appName());
        builder.put(CommonLogAttributes.SCM_RUNTIME, context.runtime());
    }

    public String correlationType(String requestedValue) {
        return requestedValue == null || requestedValue.isBlank()
                ? CorrelationType.UNKNOWN.value()
                : requestedValue.trim().toLowerCase(Locale.ROOT);
    }

    private String textOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private void putTraceRuntimeContext(ObservationDocumentBuilder builder) {
        if (context == null) {
            return;
        }
        builder.put(CommonTraceAttributes.DEPLOYMENT_SERVICE_NAME, context.appName());
        builder.put(CommonTraceAttributes.DEPLOYMENT_SERVICE_VERSION, context.serviceVersion());
        builder.put(CommonTraceAttributes.DEPLOYMENT_ENVIRONMENT, context.appProfile());
        builder.put(CommonTraceAttributes.SERVICE_NAME, context.appName());
        builder.put(CommonTraceAttributes.SCM_RUNTIME, context.runtime());
    }

    private void putAuditRuntimeContext(ObservationDocumentBuilder builder) {
        if (context == null) {
            return;
        }
        builder.put(ServiceExecuteAuditAttributes.DEPLOYMENT_SERVICE_NAME, context.appName());
        builder.put(ServiceExecuteAuditAttributes.DEPLOYMENT_SERVICE_VERSION, context.serviceVersion());
        builder.put(ServiceExecuteAuditAttributes.DEPLOYMENT_ENVIRONMENT, context.appProfile());
        builder.put(ServiceExecuteAuditAttributes.SERVICE_NAME, context.appName());
        builder.put(ServiceExecuteAuditAttributes.SCM_RUNTIME, context.runtime());
    }
}
