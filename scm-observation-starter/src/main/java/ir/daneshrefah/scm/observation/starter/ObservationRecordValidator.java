package ir.daneshrefah.scm.observation.starter;

import ir.daneshrefah.scm.observation.starter.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.starter.attributes.audit.ServiceExecuteAuditAttributes;

import java.time.DateTimeException;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
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
        validateTraceFields(stream, document);
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
        require(stream, document, "scm.metadata.namespace");
        require(stream, document, "scm.metadata.instance_id");
        require(stream, document, "scm.metadata.time_zone");
        require(stream, document, "scm.config.label");
        require(stream, document, "scm.observation.target.index");
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

    private void validateTraceFields(ObservationStream stream, Map<String, Object> document) {
        if (stream != ObservationStream.TRACE) {
            return;
        }
        if (document.containsKey("event.category")) {
            throw new IllegalStateException(
                    "Invalid TRACE observation attribute event.category: event.stream is the only trace stream identifier");
        }
        requireNonNegativeNumber(stream, "span.duration_ms", document.get("span.duration_ms"));
        validateSpanEvents(document.get("span.events"));
    }

    private void validateSpanEvents(Object value) {
        if (value == null) {
            return;
        }
        if (!(value instanceof List<?> events)) {
            throw new IllegalStateException("Invalid TRACE span.events: an ordered collection is required");
        }
        int index = 0;
        boolean awaitingProviderResponse = false;
        for (Object item : events) {
            if (!(item instanceof Map<?, ?> event)) {
                throw new IllegalStateException("Invalid TRACE span.events[" + index + "]: an event object is required");
            }
            String name = requiredEventText(event, "name", index);
            String timestamp = requiredEventText(event, "timestamp", index);
            try {
                Instant.parse(timestamp);
            } catch (DateTimeException exception) {
                throw new IllegalStateException(
                        "Invalid TRACE span.events[" + index + "].timestamp: an ISO-8601 UTC instant is required",
                        exception
                );
            }
            Object attributesValue = event.get("attributes");
            if (!(attributesValue instanceof Map<?, ?> attributes)) {
                throw new IllegalStateException(
                        "Invalid TRACE span.events[" + index + "].attributes: an attribute object is required");
            }
            if ("plugin.execute".equals(name)) {
                requireNonNegativeNumber(
                        ObservationStream.TRACE,
                        "span.events[" + index + "].attributes.plugin.duration_ms",
                        attributes.get("plugin.duration_ms")
                );
                if (missing(attributes.get("event.outcome"))) {
                    throw new IllegalStateException(
                            "Missing required TRACE observation attribute: span.events[" + index
                                    + "].attributes.event.outcome");
                }
            }
            if ("provider.request".equals(name)) {
                if (awaitingProviderResponse) {
                    throw new IllegalStateException(
                            "Invalid TRACE provider event ordering: provider.request at span.events[" + index
                                    + "] appears before the previous provider.response");
                }
                validateProviderRequestEvent(attributes, index);
                awaitingProviderResponse = true;
            }
            if ("provider.response".equals(name)) {
                if (!awaitingProviderResponse) {
                    throw new IllegalStateException(
                            "Invalid TRACE provider event ordering: provider.response at span.events[" + index
                                    + "] has no matching provider.request");
                }
                validateProviderResponseEvent(attributes, index);
                awaitingProviderResponse = false;
            }
            index++;
        }
        if (awaitingProviderResponse) {
            throw new IllegalStateException(
                    "Invalid TRACE provider event ordering: provider.request has no matching provider.response");
        }
    }

    private void validateProviderRequestEvent(Map<?, ?> attributes, int index) {
        if (missing(attributes.get("event.outcome"))) {
            throw new IllegalStateException(
                    "Missing required TRACE observation attribute: span.events[" + index
                            + "].attributes.event.outcome");
        }
        rejectPresent(attributes, "provider.duration_ms", index, "provider.request");
        rejectPresent(attributes, "provider.response_code", index, "provider.request");
        rejectPresent(attributes, "provider.error_code", index, "provider.request");
    }

    private void validateProviderResponseEvent(Map<?, ?> attributes, int index) {
        requireNonNegativeNumber(
                ObservationStream.TRACE,
                "span.events[" + index + "].attributes.provider.duration_ms",
                attributes.get("provider.duration_ms")
        );
        Object outcome = attributes.get("event.outcome");
        if (missing(outcome)) {
            throw new IllegalStateException(
                    "Missing required TRACE observation attribute: span.events[" + index
                            + "].attributes.event.outcome");
        }
        if ("failure".equalsIgnoreCase(String.valueOf(outcome).trim())
                && missing(attributes.get("provider.error_code"))
                && missing(attributes.get("error.code"))
                && missing(attributes.get("error.type"))
                && missing(attributes.get("provider.response_code"))) {
            throw new IllegalStateException(
                    "Invalid TRACE provider.response failure at span.events[" + index
                            + "]: a safe failure indicator is required");
        }
    }

    private void rejectPresent(Map<?, ?> attributes, String fieldName, int index, String eventName) {
        if (!missing(attributes.get(fieldName))) {
            throw new IllegalStateException(
                    "Invalid TRACE " + eventName + " event at span.events[" + index
                            + "]: " + fieldName + " is response-only");
        }
    }

    private String requiredEventText(Map<?, ?> event, String fieldName, int index) {
        Object value = event.get(fieldName);
        if (missing(value)) {
            throw new IllegalStateException(
                    "Missing required TRACE observation attribute: span.events[" + index + "]." + fieldName);
        }
        return String.valueOf(value).trim();
    }

    private void requireNonNegativeNumber(ObservationStream stream, String fieldName, Object value) {
        if (!(value instanceof Number number)
                || !Double.isFinite(number.doubleValue())
                || number.doubleValue() < 0D) {
            throw new IllegalStateException(
                    "Invalid " + stream + " observation attribute " + fieldName + ": a non-negative number is required");
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
