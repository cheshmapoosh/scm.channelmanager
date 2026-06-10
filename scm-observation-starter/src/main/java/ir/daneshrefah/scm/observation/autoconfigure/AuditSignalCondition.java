package ir.daneshrefah.scm.observation.autoconfigure;

final class AuditSignalCondition extends ObservationSignalConditionSupport {
    @Override
    protected String[] signalProperties() {
        return new String[]{"scm.observation.audit.enabled"};
    }
}
