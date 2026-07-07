package ir.daneshrefah.scm.observation;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ObservationDocumentBuilder {
    private final ObservationStream stream;
    private final ObservationContext context;
    private final ObsTargetIndexResolver targetIndexResolver;
    private final ObservationAttributeRegistry registry;
    private final ObservationSanitizer sanitizer;
    private final LinkedHashMap<String, Object> document = new LinkedHashMap<>();

    ObservationDocumentBuilder(
            ObservationStream stream,
            ObservationContext context,
            ObsTargetIndexResolver targetIndexResolver,
            ObservationAttributeRegistry registry,
            ObservationSanitizer sanitizer
    ) {
        this.stream = stream;
        this.context = context;
        this.targetIndexResolver = targetIndexResolver == null ? new ObsTargetIndexResolver() : targetIndexResolver;
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
        putStandardFields();
        return new LinkedHashMap<>(document);
    }

    private void putStandardFields() {
        if (stream == null || context == null) {
            return;
        }
        Instant timestamp = timestamp();
        String channelCode = firstText(document.get("scm.channel.code"), context.channelCode());
        String namespace = targetIndexResolver.normalizeNamespace(context.namespace());
        String platform = targetIndexResolver.normalizePlatform(context.platform());
        String environment = targetIndexResolver.normalizeEnvironment(context.appProfile());
        put("event.stream", stream.value());
        put("scm.obs.target.namespace", namespace);
        put("scm.platform", platform);
        put("service.name", context.appName());
        put("deployment.environment", environment);
        put("scm.obs.legacy.enabled", document.getOrDefault("scm.obs.legacy.enabled", Boolean.FALSE));
        put("scm.obs.target.index", targetIndexResolver.resolve(
                stream,
                platform,
                namespace,
                environment,
                channelCode,
                timestamp,
                context.observationZoneId()
        ));
    }

    private Instant timestamp() {
        Object value = document.get("@timestamp");
        if (value == null) {
            return Instant.now();
        }
        try {
            return Instant.parse(String.valueOf(value));
        } catch (DateTimeParseException ignored) {
            return Instant.now();
        }
    }

    private String firstText(Object first, String second) {
        if (first != null && !String.valueOf(first).isBlank()) {
            return String.valueOf(first).trim();
        }
        return second;
    }
}
