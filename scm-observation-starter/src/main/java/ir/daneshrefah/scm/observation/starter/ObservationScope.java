package ir.daneshrefah.scm.observation.starter;

import ir.daneshrefah.scm.observation.starter.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObservationScope implements AutoCloseable {
    private static final ThreadLocal<ObservationScope> CURRENT = new ThreadLocal<>();

    private final TraceObservationHandle traceHandle;
    private final AutoCloseable contextScope;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private final ObservationScope previousScope;
    private String outcome;
    private Throwable throwable;
    private volatile boolean closed;

    ObservationScope(TraceObservationHandle traceHandle) {
        this(traceHandle, null);
    }

    ObservationScope(TraceObservationHandle traceHandle, AutoCloseable contextScope) {
        this.traceHandle = traceHandle == null ? TraceObservationHandle.NOOP : traceHandle;
        this.contextScope = contextScope;
        this.previousScope = current();
        CURRENT.set(this);
    }

    public static ObservationScope current() {
        ObservationScope scope = CURRENT.get();
        ObservationScope active = active(scope);
        if (active != scope) {
            if (active == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(active);
            }
        }
        return active;
    }

    public synchronized ObservationScope success() {
        if (!closed) {
            this.outcome = "success";
        }
        return this;
    }

    public synchronized ObservationScope failure() {
        if (!closed) {
            this.outcome = "failure";
        }
        return this;
    }

    public synchronized ObservationScope failure(Throwable throwable) {
        if (!closed) {
            this.outcome = "failure";
            this.throwable = throwable;
            if (throwable != null) {
                attributes.put(CommonTraceAttributes.ERROR_TYPE.name(), throwable.getClass().getName());
                if (throwable.getMessage() != null) {
                    attributes.put(CommonTraceAttributes.ERROR_MESSAGE.name(), throwable.getMessage());
                }
            }
        }
        return this;
    }

    public synchronized ObservationScope outcome(String outcome) {
        if (!closed) {
            this.outcome = outcome;
        }
        return this;
    }

    public synchronized ObservationScope attribute(String name, Object value) {
        if (!closed && name != null && !name.isBlank() && value != null) {
            attributes.put(name, value);
        }
        return this;
    }

    public <V> ObservationScope attribute(ObservationAttributeKey<V> key, V value) {
        if (key != null) {
            attribute(key.name(), value);
        }
        return this;
    }

    public synchronized ObservationScope attributes(Map<String, ?> values) {
        if (!closed && values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                attribute(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public synchronized ObservationScope event(String name, Map<String, ?> attributes) {
        if (!closed && name != null && !name.isBlank()) {
            traceHandle.event(name.trim(), attributes);
        }
        return this;
    }

    @Override
    public void close() {
        Map<String, Object> finalAttributes;
        String finalOutcome;
        Throwable finalThrowable;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            finalAttributes = new LinkedHashMap<>(attributes);
            finalOutcome = outcome;
            finalThrowable = throwable;
        }
        try {
            traceHandle.finish(finalOutcome, finalAttributes, finalThrowable);
        } finally {
            restorePreviousScope();
            closeContextScope();
        }
    }

    private void restorePreviousScope() {
        if (CURRENT.get() != this) {
            return;
        }
        ObservationScope previous = active(previousScope);
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    private static ObservationScope active(ObservationScope scope) {
        ObservationScope active = scope;
        while (active != null && active.closed) {
            active = active.previousScope;
        }
        return active;
    }

    private void closeContextScope() {
        if (contextScope == null) {
            return;
        }
        try {
            contextScope.close();
        } catch (Exception ignored) {
            // Restoring ThreadLocal trace context must not hide the original observation close result.
        }
    }
}
