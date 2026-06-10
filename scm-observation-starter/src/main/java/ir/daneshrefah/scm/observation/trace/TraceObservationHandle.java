package ir.daneshrefah.scm.observation.trace;

import java.util.Map;

public interface TraceObservationHandle {
    TraceObservationHandle NOOP = (outcome, attributes, throwable) -> {
    };

    void finish(String outcome, Map<String, Object> attributes, Throwable throwable);
}
