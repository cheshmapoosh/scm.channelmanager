package ir.daneshrefah.scm.observation.trace;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

record TraceObservationSpanEvent(
        String name,
        Instant timestamp,
        Map<String, Object> attributes
) {
    TraceObservationSpanEvent {
        name = name == null || name.isBlank() ? "span.event" : name.trim();
        timestamp = timestamp == null ? Instant.now() : timestamp;
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    Map<String, Object> toDocument() {
        Map<String, Object> document = new LinkedHashMap<>();
        document.put("name", name);
        document.put("timestamp", timestamp.toString());
        document.put("attributes", attributes);
        return document;
    }
}
