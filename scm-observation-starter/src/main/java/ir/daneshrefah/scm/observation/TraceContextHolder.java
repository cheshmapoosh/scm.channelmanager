package ir.daneshrefah.scm.observation;

public final class TraceContextHolder {
    private static final ThreadLocal<TraceContext> CURRENT = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    public static TraceContext current() {
        return CURRENT.get();
    }

    public static void set(TraceContext context) {
        if (context == null) {
            clear();
            return;
        }
        CURRENT.set(context);
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Scope open(TraceContext context) {
        TraceContext previous = current();
        set(context);
        return new Scope(previous);
    }

    public static TraceContext childContext() {
        return childContext(current(), null, null, null, null);
    }

    public static TraceContext childContext(
            TraceContext parent,
            String traceId,
            String spanId,
            String correlationId,
            String correlationType
    ) {
        return new TraceContext(
                firstText(traceId, parent == null ? null : parent.traceId(), ObservationIds.traceId()),
                firstText(spanId, ObservationIds.spanId()),
                firstText(correlationId, parent == null ? null : parent.correlationId(), ObservationIds.correlationId()),
                firstText(correlationType, parent == null ? null : parent.correlationType(), CorrelationType.OPERATION.value())
        );
    }

    private static String firstText(String... values) {
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    public static final class Scope implements AutoCloseable {
        private final TraceContext previous;
        private boolean closed;

        private Scope(TraceContext previous) {
            this.previous = previous;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }
            closed = true;
            set(previous);
        }
    }
}
