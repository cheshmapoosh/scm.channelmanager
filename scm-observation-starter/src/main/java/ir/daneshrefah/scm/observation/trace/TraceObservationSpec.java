package ir.daneshrefah.scm.observation.trace;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record TraceObservationSpec(
        Class<?> sourceClass,
        String spanName,
        String spanKind,
        String action,
        String outcome,
        String correlationId,
        String traceId,
        String spanId,
        String parentSpanId,
        Map<String, Object> attributes
) {
    public TraceObservationSpec {
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }
}
