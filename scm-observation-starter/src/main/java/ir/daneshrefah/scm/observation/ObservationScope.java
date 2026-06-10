package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.trace.TraceObservationHandle;
import ir.daneshrefah.scm.observation.attributes.ScmErrorAttributes;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObservationScope implements AutoCloseable {
    private final TraceObservationHandle traceHandle;
    private final Map<String, Object> attributes = new LinkedHashMap<>();
    private String outcome;
    private Throwable throwable;
    private boolean closed;

    ObservationScope(TraceObservationHandle traceHandle) {
        this.traceHandle = traceHandle == null ? TraceObservationHandle.NOOP : traceHandle;
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
            attribute(ScmErrorAttributes.TYPE, throwable.getClass().getName());
            attribute(ScmErrorAttributes.MESSAGE, throwable.getMessage());
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
            traceHandle.finish(outcome, attributes, throwable);
        }
    }
}
