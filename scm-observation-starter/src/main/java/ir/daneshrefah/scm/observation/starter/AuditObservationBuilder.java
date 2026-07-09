package ir.daneshrefah.scm.observation.starter;

import ir.daneshrefah.scm.observation.starter.attributes.audit.ChangeEntityAuditAttributes;
import ir.daneshrefah.scm.observation.starter.attributes.audit.ServiceExecuteAuditAttributes;
import ir.daneshrefah.scm.observation.starter.policy.ObservationSignal;

import java.time.Instant;
import java.util.LinkedHashMap;

public class AuditObservationBuilder extends AbstractObservationBuilder<AuditObservationBuilder> {
    private String auditType = ServiceExecuteAuditAttributes.TYPE_VALUE;
    private String category = "service";
    private String userName;
    private String resourceType;
    private String resourceId;
    private ObservationRecordKind recordKind = ObservationRecordKind.EVENT;
    private Throwable throwable;

    AuditObservationBuilder(ScmObservation observation) {
        super(observation);
        this.action = "service.audit";
    }

    public AuditObservationBuilder service() {
        this.auditType = ServiceExecuteAuditAttributes.TYPE_VALUE;
        this.category = "service";
        this.action = "service.audit";
        this.recordKind = ObservationRecordKind.EVENT;
        return this;
    }

    public AuditObservationBuilder change() {
        this.auditType = ChangeEntityAuditAttributes.TYPE_VALUE;
        this.category = "configuration";
        this.action = "change.audit";
        this.recordKind = ObservationRecordKind.CHANGE;
        return this;
    }

    public AuditObservationBuilder userName(String userName) {
        this.userName = userName;
        return this;
    }

    public AuditObservationBuilder resource(String resourceType, String resourceId) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        return this;
    }

    public AuditObservationBuilder failure(Throwable throwable) {
        outcome(OUTCOME_FAILURE);
        this.throwable = throwable;
        if (throwable != null) {
            attribute(ServiceExecuteAuditAttributes.ERROR_TYPE, throwable.getClass().getName());
            attribute(ServiceExecuteAuditAttributes.ERROR_MESSAGE, safeMessage(throwable));
        }
        return this;
    }

    public void write() {
        if (!observation.isEnabled(ObservationSignal.AUDIT)) {
            return;
        }
        Instant timestamp = observation.now();
        ObservationDocumentBuilder builder = observation.documentFactory().audit(
                timestamp,
                action,
                correlationId,
                CorrelationType.OPERATION.value()
        );
        builder.put(ServiceExecuteAuditAttributes.EVENT_CATEGORY, category);
        builder.put(ServiceExecuteAuditAttributes.EVENT_ACTION, action);
        builder.put(ServiceExecuteAuditAttributes.EVENT_OUTCOME, outcome);
        builder.put(ServiceExecuteAuditAttributes.AUDIT_TYPE, auditType);
        if (ChangeEntityAuditAttributes.TYPE_VALUE.equals(auditType)) {
            builder.put(ChangeEntityAuditAttributes.ACTOR_USERNAME_MASKED, userName);
            builder.put(ChangeEntityAuditAttributes.ENTITY_TYPE, resourceType);
            builder.put(ChangeEntityAuditAttributes.ENTITY_ID, resourceId);
        } else {
            builder.put(ServiceExecuteAuditAttributes.ACTOR_USERNAME_MASKED, userName);
            builder.put(ServiceExecuteAuditAttributes.RESOURCE_TYPE, resourceType);
            builder.put(ServiceExecuteAuditAttributes.RESOURCE_ID, resourceId);
        }
        builder.putAll(attributes);
        LinkedHashMap<String, Object> document = builder.build();
        observation.validate(ObservationStream.AUDIT, recordKind, throwable != null, document);
        observation.write(ObservationEventSignal.AUDIT, sourceClass, document);
    }

    @Override
    protected AuditObservationBuilder self() {
        return this;
    }

    private String safeMessage(Throwable throwable) {
        if (throwable == null || throwable.getMessage() == null) {
            return null;
        }
        String message = throwable.getMessage().replace('\r', ' ').replace('\n', ' ').trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    private static final String OUTCOME_FAILURE = "failure";
}
