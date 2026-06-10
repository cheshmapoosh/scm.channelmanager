package ir.daneshrefah.scm.observation.trace;

public class NoopTraceObservationSink implements TraceObservationSink {
    @Override
    public TraceObservationHandle start(TraceObservationSpec spec) {
        return TraceObservationHandle.NOOP;
    }
}
