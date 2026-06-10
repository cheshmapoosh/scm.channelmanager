package ir.daneshrefah.scm.observation.autoconfigure;

final class TraceSignalCondition extends ObservationSignalConditionSupport {
    @Override
    protected String[] signalProperties() {
        return new String[]{"scm.observation.trace.enabled"};
    }
}
