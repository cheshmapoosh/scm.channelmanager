package ir.daneshrefah.scm.observation;

public record TraceContext(
        String traceId,
        String spanId,
        String correlationId,
        String correlationType
) {
    public TraceContext {
        traceId = textOrNull(traceId);
        spanId = textOrNull(spanId);
        correlationId = textOrNull(correlationId);
        correlationType = textOrNull(correlationType);
    }

    private static String textOrNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
