package ir.daneshrefah.scm.observation.starter.trace;

public class NoopTraceObservationSink implements TraceObservationSink {
    @Override
    public TraceObservationHandle start(TraceObservationSpec spec) {
        return TraceObservationHandle.NOOP;
    }
}
