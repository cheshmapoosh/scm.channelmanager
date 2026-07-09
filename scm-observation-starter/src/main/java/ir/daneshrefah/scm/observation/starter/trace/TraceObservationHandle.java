package ir.daneshrefah.scm.observation.starter.trace;

import java.util.Map;

public interface TraceObservationHandle {
    TraceObservationHandle NOOP = (outcome, attributes, throwable) -> {
    };

    default void event(String name, Map<String, ?> attributes) {
        // Default no-op for disabled tracing or implementations without span-event support.
    }

    void finish(String outcome, Map<String, Object> attributes, Throwable throwable);
}
