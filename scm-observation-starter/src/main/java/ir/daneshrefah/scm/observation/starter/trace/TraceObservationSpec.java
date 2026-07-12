package ir.daneshrefah.scm.observation.starter.trace;

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
        String correlationType,
        String traceId,
        String spanId,
        String parentSpanId,
        String traceFlags,
        Map<String, Object> attributes
) {
    public TraceObservationSpec {
        attributes = attributes == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }
}
