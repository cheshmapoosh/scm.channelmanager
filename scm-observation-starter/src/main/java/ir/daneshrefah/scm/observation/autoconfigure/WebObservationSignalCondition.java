package ir.daneshrefah.scm.observation.autoconfigure;

final class WebObservationSignalCondition extends ObservationSignalConditionSupport {
    @Override
    protected String[] signalProperties() {
        return new String[]{
                "scm.observation.trace.enabled",
                "scm.observation.audit.enabled",
                "scm.observation.metric.enabled"
        };
    }
}
