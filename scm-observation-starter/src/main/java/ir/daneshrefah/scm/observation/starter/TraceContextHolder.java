package ir.daneshrefah.scm.observation.starter;

import java.util.concurrent.atomic.AtomicBoolean;

public final class TraceContextHolder {
    private static final ThreadLocal<Binding> CURRENT = new ThreadLocal<>();

    private TraceContextHolder() {
    }

    public static TraceContext current() {
        Binding binding = activeBinding();
        return binding == null ? null : binding.context;
    }

    public static void set(TraceContext context) {
        if (context == null) {
            clear();
            return;
        }
        CURRENT.set(new Binding(context, null));
    }

    public static void clear() {
        CURRENT.remove();
    }

    public static Scope open(TraceContext context) {
        Binding binding = new Binding(context, activeBinding());
        CURRENT.set(binding);
        return new Scope(binding);
    }

    public static TraceContext childContext() {
        return childContext(current(), null, null, null, null, null);
    }

    public static TraceContext childContext(
            TraceContext parent,
            String traceId,
            String spanId,
            String correlationId,
            String correlationType
    ) {
        return childContext(parent, traceId, spanId, correlationId, correlationType, null);
    }

    public static TraceContext childContext(
            TraceContext parent,
            String traceId,
            String spanId,
            String correlationId,
            String correlationType,
            String traceFlags
    ) {
        return new TraceContext(
                firstText(traceId, parent == null ? null : parent.traceId(), ObservationIds.traceId()),
                firstText(spanId, ObservationIds.spanId()),
                firstText(correlationId, parent == null ? null : parent.correlationId(), ObservationIds.correlationId()),
                firstText(correlationType, parent == null ? null : parent.correlationType(), CorrelationType.OPERATION.value()),
                firstText(traceFlags, parent == null ? null : parent.traceFlags(), TraceFlags.DEFAULT)
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

    private static Binding activeBinding() {
        Binding current = CURRENT.get();
        Binding active = active(current);
        if (active != current) {
            restore(active);
        }
        return active;
    }

    private static Binding active(Binding binding) {
        Binding active = binding;
        while (active != null && active.closed.get()) {
            active = active.previous;
        }
        return active;
    }

    private static void restore(Binding binding) {
        Binding active = active(binding);
        if (active == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(active);
        }
    }

    private static final class Binding {
        private final TraceContext context;
        private final Binding previous;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Binding(TraceContext context, Binding previous) {
            this.context = context;
            this.previous = previous;
        }
    }

    public static final class Scope implements AutoCloseable {
        private final Binding binding;

        private Scope(Binding binding) {
            this.binding = binding;
        }

        @Override
        public void close() {
            if (binding == null || !binding.closed.compareAndSet(false, true)) {
                return;
            }
            if (CURRENT.get() == binding) {
                restore(binding.previous);
            }
        }
    }
}
