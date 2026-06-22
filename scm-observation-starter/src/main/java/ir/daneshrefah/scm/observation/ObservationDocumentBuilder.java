package ir.daneshrefah.scm.observation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObservationDocumentBuilder {
    private final ObservationStream stream;
    private final ObservationRecordKind kind;
    private final boolean errorContext;
    private final ObservationAttributeRegistry registry;
    private final ObservationSanitizer sanitizer;
    private final ObservationRecordValidator validator;
    private final LinkedHashMap<String, Object> document = new LinkedHashMap<>();

    ObservationDocumentBuilder(
            ObservationStream stream,
            ObservationRecordKind kind,
            boolean errorContext,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer,
            ObservationRecordValidator validator
    ) {
        this.stream = stream;
        this.kind = kind == null ? ObservationRecordKind.PLAIN : kind;
        this.errorContext = errorContext || this.kind == ObservationRecordKind.EXCEPTION;
        this.registry = registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
        this.sanitizer = sanitizer;
        this.validator = validator == null ? new ObservationRecordValidator(this.registry) : validator;
    }

    public ObservationDocumentBuilder put(String fieldName, Object value) {
        if (fieldName == null || fieldName.isBlank() || value == null) {
            return this;
        }
        String normalizedField = fieldName.trim();
        Object sanitized = sanitizer == null ? value : sanitizer.sanitize(normalizedField, value);
        Object prepared = registry.prepareValue(stream, normalizedField, sanitized);
        if (prepared != null) {
            document.put(normalizedField, prepared);
        }
        return this;
    }

    public <V> ObservationDocumentBuilder put(ObservationAttributeKey<V> key, V value) {
        if (key != null) {
            put(key.name(), value);
        }
        return this;
    }

    public ObservationDocumentBuilder putAll(Map<String, ?> values) {
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public Map<String, Object> snapshot() {
        return Collections.unmodifiableMap(document);
    }

    public LinkedHashMap<String, Object> build() {
        validator.validate(stream, kind, errorContext, document);
        return new LinkedHashMap<>(document);
    }
}
