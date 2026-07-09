package ir.daneshrefah.scm.observation.starter.trace;

public interface TraceObservationSink {
    TraceObservationHandle start(TraceObservationSpec spec);
}
