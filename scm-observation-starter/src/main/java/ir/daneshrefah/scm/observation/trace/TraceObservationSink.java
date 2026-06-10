package ir.daneshrefah.scm.observation.trace;

public interface TraceObservationSink {
    TraceObservationHandle start(TraceObservationSpec spec);
}
