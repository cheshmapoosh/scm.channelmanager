package ir.daneshrefah.scm.observation.starter.trace;

import java.lang.reflect.Array;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

record TraceObservationSpanEvent(
        String name,
        Instant timestamp,
        Map<String, Object> attributes
) {
    TraceObservationSpanEvent {
        name = name == null || name.isBlank() ? "span.event" : name.trim();
        timestamp = timestamp == null ? Instant.now() : timestamp;
        attributes = immutableAttributes(attributes);
    }

    Map<String, Object> toDocument() {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("name", name);
        document.put("timestamp", timestamp.toString());
        document.put("attributes", attributes);
        return Collections.unmodifiableMap(document);
    }

    private static Map<String, Object> immutableAttributes(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null && value != null) {
                copy.put(key, immutableValue(value));
            }
        });
        return Collections.unmodifiableMap(copy);
    }

    private static Object immutableValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((key, item) -> {
                if (key != null && item != null) {
                    copy.put(String.valueOf(key), immutableValue(item));
                }
            });
            return Collections.unmodifiableMap(copy);
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> copy = new ArrayList<>();
            iterable.forEach(item -> {
                if (item != null) {
                    copy.add(immutableValue(item));
                }
            });
            return Collections.unmodifiableList(copy);
        }
        if (value.getClass().isArray()) {
            List<Object> copy = new ArrayList<>();
            for (int index = 0; index < Array.getLength(value); index++) {
                Object item = Array.get(value, index);
                if (item != null) {
                    copy.add(immutableValue(item));
                }
            }
            return Collections.unmodifiableList(copy);
        }
        return value;
    }
}
