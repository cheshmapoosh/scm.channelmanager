package ir.daneshrefah.scm.observation.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

final class TraceObservationSpanEventRegistry {
    private static final ConcurrentMap<String, List<TraceObservationSpanEvent>> EVENTS = new ConcurrentHashMap<>();

    private TraceObservationSpanEventRegistry() {
    }

    static void add(String traceId, String spanId, TraceObservationSpanEvent event) {
        String key = key(traceId, spanId);
        if (key == null || event == null) {
            return;
        }
        EVENTS.compute(key, (ignored, existing) -> {
            List<TraceObservationSpanEvent> events = existing == null ? new ArrayList<>() : new ArrayList<>(existing);
            events.add(event);
            return List.copyOf(events);
        });
    }

    static List<Map<String, Object>> drain(String traceId, String spanId) {
        String key = key(traceId, spanId);
        if (key == null) {
            return List.of();
        }
        List<TraceObservationSpanEvent> events = EVENTS.remove(key);
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        return events.stream()
                .map(TraceObservationSpanEvent::toDocument)
                .toList();
    }

    private static String key(String traceId, String spanId) {
        if (traceId == null || traceId.isBlank() || spanId == null || spanId.isBlank()) {
            return null;
        }
        return traceId.trim() + ":" + spanId.trim();
    }
}
