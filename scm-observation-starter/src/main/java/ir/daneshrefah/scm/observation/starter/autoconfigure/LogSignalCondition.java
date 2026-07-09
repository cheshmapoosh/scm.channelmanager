package ir.daneshrefah.scm.observation.starter.autoconfigure;

final class LogSignalCondition extends ObservationSignalConditionSupport {
    @Override
    protected String[] signalProperties() {
        return new String[]{"scm.observation.log.enabled"};
    }
}
