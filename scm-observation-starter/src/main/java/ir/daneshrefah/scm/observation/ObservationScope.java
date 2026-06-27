package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.attributes.trace.CommonTraceAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObservationScope implements AutoCloseable {
    private final TraceObservationHandle traceHandle;
    private final AutoCloseable contextScope;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private String outcome;
    private Throwable throwable;
    private boolean closed;

    ObservationScope(TraceObservationHandle traceHandle) {
        this(traceHandle, null);
    }

    ObservationScope(TraceObservationHandle traceHandle, AutoCloseable contextScope) {
        this.traceHandle = traceHandle == null ? TraceObservationHandle.NOOP : traceHandle;
        this.contextScope = contextScope;
    }

    public ObservationScope success() {
        this.outcome = "success";
        return this;
    }

    public ObservationScope failure() {
        this.outcome = "failure";
        return this;
    }

    public ObservationScope failure(Throwable throwable) {
        this.outcome = "failure";
        this.throwable = throwable;
        if (throwable != null) {
            attribute(CommonTraceAttributes.ERROR_TYPE, throwable.getClass().getName());
            attribute(CommonTraceAttributes.ERROR_MESSAGE, throwable.getMessage());
        }
        return this;
    }

    public ObservationScope outcome(String outcome) {
        this.outcome = outcome;
        return this;
    }

    public ObservationScope attribute(String name, Object value) {
        if (name != null && !name.isBlank() && value != null) {
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

    public ObservationScope attributes(Map<String, ?> values) {
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                attribute(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    @Override
    public void close() {
        if (!closed) {
            closed = true;
            try {
                traceHandle.finish(outcome, attributes, throwable);
            } finally {
                closeContextScope();
            }
        }
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
