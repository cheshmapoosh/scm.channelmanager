package ir.daneshrefah.scm.observation.starter.autoconfigure;

final class MetricSignalCondition extends ObservationSignalConditionSupport {
    @Override
    protected String[] signalProperties() {
        return new String[]{"scm.observation.metric.enabled"};
    }
}
