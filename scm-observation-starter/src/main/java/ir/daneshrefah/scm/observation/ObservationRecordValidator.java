package ir.daneshrefah.scm.observation;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ObservationRecordValidator {
    private static final Set<String> ALLOWED_CORRELATION_TYPES = Set.of(
            "lifecycle",
            "request",
            "message",
            "job",
            "batch",
            "operation",
            "unknown"
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
        String normalized = String.valueOf(value).trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CORRELATION_TYPES.contains(normalized)) {
            throw new IllegalStateException("Invalid " + stream + " correlation.type: " + value);
        }
    }

    private boolean missing(Object value) {
        return value == null || (value instanceof String text && text.isBlank());
    }
}
