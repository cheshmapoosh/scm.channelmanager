package ir.daneshrefah.scm.observation.starter.trace;

import ir.daneshrefah.scm.observation.starter.TraceContext;

import java.util.Map;

public interface TraceObservationHandle {
    TraceObservationHandle NOOP = (outcome, attributes, throwable) -> {
    };

    default void event(String name, Map<String, ?> attributes) {
        // Default no-op for disabled tracing or implementations without span-event support.
    }

    default TraceContext traceContext() {
        return null;
    }

    void finish(String outcome, Map<String, Object> attributes, Throwable throwable);
}
