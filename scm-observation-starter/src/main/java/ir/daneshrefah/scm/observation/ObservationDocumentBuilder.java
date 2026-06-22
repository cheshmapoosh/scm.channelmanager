package ir.daneshrefah.scm.observation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObservationDocumentBuilder {
    private final ObservationStream stream;
    private final ObservationAttributeRegistry registry;
    private final ObservationSanitizer sanitizer;
    private final LinkedHashMap<String, Object> document = new LinkedHashMap<>();

    ObservationDocumentBuilder(
            ObservationStream stream,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer
    ) {
        this.stream = stream;
        this.registry = registry == null ? ObservationAttributeRegistry.commonOnly() : registry;
        this.sanitizer = sanitizer;
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
        return new LinkedHashMap<>(document);
    }
}
