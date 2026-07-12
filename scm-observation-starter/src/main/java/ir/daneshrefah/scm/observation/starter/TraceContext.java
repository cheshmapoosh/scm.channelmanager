package ir.daneshrefah.scm.observation.starter;

public record TraceContext(
        String traceId,
        String spanId,
        String correlationId,
        String correlationType,
        String traceFlags
) {
    public TraceContext {
        traceId = textOrNull(traceId);
        spanId = textOrNull(spanId);
        correlationId = textOrNull(correlationId);
        correlationType = textOrNull(correlationType);
        traceFlags = TraceFlags.normalizeOrDefault(traceFlags);
    }

    public TraceContext(
            String traceId,
            String spanId,
            String correlationId,
            String correlationType
    ) {
        this(traceId, spanId, correlationId, correlationType, TraceFlags.DEFAULT);
    }

    private static String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
