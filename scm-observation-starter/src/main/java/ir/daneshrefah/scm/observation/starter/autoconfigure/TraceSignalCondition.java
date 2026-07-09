package ir.daneshrefah.scm.observation.starter.autoconfigure;

final class TraceSignalCondition extends ObservationSignalConditionSupport {
    @Override
    protected String[] signalProperties() {
        return new String[]{"scm.observation.trace.enabled"};
    }
}
