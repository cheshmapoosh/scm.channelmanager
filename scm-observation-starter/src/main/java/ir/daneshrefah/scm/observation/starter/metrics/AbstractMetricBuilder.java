package ir.daneshrefah.scm.observation.starter.metrics;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationSanitizer;
import ir.daneshrefah.scm.observation.starter.attributes.metric.MetricTag;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

abstract class AbstractMetricBuilder<T extends AbstractMetricBuilder<T>> {
    private static final Set<String> DISALLOWED_TAGS = Set.of(
            "correlation.id",
            "scm.correlation_id",
            "trace.id",
            "span.id",
            "parent.span.id",
            "scm.card.no",
            "scm.account.no",
            "scm.destination",
            "scm.client.phone_number",
            "username",
            "user.name",
            "token",
            "otp",
            "message.sequence.id",
            "scm.message.sequence_id",
            "authorization",
            "scm.auth.jwt.hash",
            "uaa.jwt.masked"
    );

    protected final MetricObservationSink sink;
    protected final ObservationSanitizer sanitizer;
    protected final String name;
    protected final Map<String, String> tags = new LinkedHashMap<>();

    AbstractMetricBuilder(MetricObservationSink sink, ObservationSanitizer sanitizer, String name) {
        this.sink = sink;
        this.sanitizer = sanitizer;
        this.name = name;
    }

    public T tag(String name, String value) {
        addTag(name, value);
        return self();
    }

    public T tag(ObservationAttributeKey<String> key, String value) {
        if (key != null) {
            addTag(key.name(), value);
        }
        return self();
    }

    public T tag(ObservationAttributeKey<?> key, Object value) {
        if (key != null && value != null) {
            addTag(key.name(), String.valueOf(value));
        }
        return self();
    }

    protected Map<String, String> tags() {
        return tags;
    }

    protected abstract T self();

    private void addTag(String name, String value) {
        if (name == null || name.isBlank() || value == null || isDisallowed(name)) {
            return;
        }
        Object sanitized = sanitizer.sanitize(name.trim(), value);
        if (sanitized instanceof String sanitizedText && !sanitizedText.isBlank()) {
            tags.put(name.trim(), sanitizedText);
        }
    }

    private boolean isDisallowed(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return MetricTag.isForbidden(normalized)
                || DISALLOWED_TAGS.contains(normalized)
                || normalized.contains("payload")
                || normalized.contains("request.body")
                || normalized.contains("response.body")
                || normalized.contains("raw_request")
                || normalized.contains("raw_response");
    }
}
