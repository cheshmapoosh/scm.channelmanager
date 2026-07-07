package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.audit.ServiceExecuteAuditAttributes;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public class ObservationRecordValidator {
    private static final Set<String> AUDIT_TYPES = Set.of(
            ChangeEntityAuditAttributes.TYPE_VALUE,
            ServiceExecuteAuditAttributes.TYPE_VALUE
    );

    private final ObservationAttributeRegistry registry;

    public ObservationRecordValidator(ObservationAttributeRegistry registry) {
        this.registry = registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
    }

    public void validate(
            ObservationStream stream,
            ObservationRecordKind kind,
            boolean errorContext,
            Map<String, Object> document
    ) {
        if (stream == null) {
            throw new IllegalArgumentException("Observation stream is required.");
        }
        if (kind == null) {
            throw new IllegalArgumentException("Observation record kind is required.");
        }
        if (document == null) {
            throw new IllegalArgumentException("Observation document is required.");
        }

        EnumSet<ObservationAttributePresence> required = requiredPresence(kind, errorContext);
        for (ObservationAttributeKey<?> key : registry.all(stream)) {
            if (required.contains(key.presence()) && missing(document.get(key.name()))) {
                throw new IllegalStateException("Missing required " + stream + " observation attribute: " + key.name());
            }
        }
        validateCorrelationType(stream, document);
        validateStandardTargetFields(stream, document);
        validateLegacyProjection(stream, document);
        validateAuditType(stream, document);
        validateTraceParent(stream, document);
    }

    private EnumSet<ObservationAttributePresence> requiredPresence(ObservationRecordKind kind, boolean errorContext) {
        EnumSet<ObservationAttributePresence> required = EnumSet.of(ObservationAttributePresence.ALWAYS_REQUIRED);
        switch (kind) {
            case CONTEXT -> required.add(ObservationAttributePresence.CONTEXT_REQUIRED);
            case EVENT -> required.add(ObservationAttributePresence.EVENT_REQUIRED);
            case EXCEPTION -> required.add(ObservationAttributePresence.ERROR_REQUIRED);
            case PLAIN, CHANGE -> {
                // No extra required presence in Cycle 0.
            }
        }
        if (errorContext) {
            required.add(ObservationAttributePresence.ERROR_REQUIRED);
        }
        return required;
    }

    private void validateCorrelationType(ObservationStream stream, Map<String, Object> document) {
        Object value = document.get("correlation.type");
        if (value == null) {
            return;
        }
        if (!CorrelationType.isAllowed(String.valueOf(value))) {
            throw new IllegalStateException("Invalid " + stream + " correlation.type: " + value);
        }
    }

    private void validateAuditType(ObservationStream stream, Map<String, Object> document) {
        if (stream != ObservationStream.AUDIT) {
            return;
        }
        Object value = document.get(ChangeEntityAuditAttributes.AUDIT_TYPE.name());
        if (value == null || !AUDIT_TYPES.contains(String.valueOf(value))) {
            throw new IllegalStateException("Invalid AUDIT audit.type: " + value);
        }
    }

    private void validateStandardTargetFields(ObservationStream stream, Map<String, Object> document) {
        require(stream, document, "event.stream");
        require(stream, document, "scm.observation.target.namespace");
        require(stream, document, "scm.observation.target.index");
        require(stream, document, "scm.platform");
        require(stream, document, "service.name");
        require(stream, document, "deployment.environment");
    }

    private void validateLegacyProjection(ObservationStream stream, Map<String, Object> document) {
        Object enabled = document.get("scm.observation.legacy.enabled");
        if (!isTrue(enabled)) {
            return;
        }
        if (missing(document.get("scm.observation.legacy.service.code"))) {
            throw new IllegalStateException("Invalid " + stream
                    + " legacy projection: scm.observation.legacy.service.code is required when scm.observation.legacy.enabled=true");
        }
        if (missing(document.get("scm.observation.legacy.operation.code"))) {
            throw new IllegalStateException("Invalid " + stream
                    + " legacy projection: scm.observation.legacy.operation.code is required when scm.observation.legacy.enabled=true");
        }
    }

    private void validateTraceParent(ObservationStream stream, Map<String, Object> document) {
        if (stream != ObservationStream.TRACE) {
            return;
        }
        Object parentSpanId = document.get("parent.span.id");
        if (missing(parentSpanId)) {
            return; // Absent, null, or blank parent.span.id defines a root span.
        }
        Object spanId = document.get("span.id");
        if (String.valueOf(parentSpanId).equals(String.valueOf(spanId))) {
            throw new IllegalStateException("Invalid TRACE parent.span.id: a span cannot be its own parent");
        }
    }

    private boolean missing(Object value) {
        return value == null || (value instanceof String text && text.isBlank());
    }

    private void require(ObservationStream stream, Map<String, Object> document, String fieldName) {
        if (missing(document.get(fieldName))) {
            throw new IllegalStateException("Missing required " + stream + " observation attribute: " + fieldName);
        }
    }

    private boolean isTrue(Object value) {
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return value != null && "true".equalsIgnoreCase(String.valueOf(value).trim());
    }
}
