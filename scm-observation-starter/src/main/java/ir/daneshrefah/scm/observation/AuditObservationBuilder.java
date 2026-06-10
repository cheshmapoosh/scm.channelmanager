package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.ScmAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmErrorAttributes;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;

import java.time.Instant;
import java.util.LinkedHashMap;

public class AuditObservationBuilder extends AbstractObservationBuilder<AuditObservationBuilder> {
    private String auditType = "SERVICE";
    private String category = "service";
    private String userName;
    private String resourceType;
    private String resourceId;

    AuditObservationBuilder(ScmObservation observation) {
        super(observation);
        this.action = "service.audit";
    }

    public AuditObservationBuilder service() {
        this.auditType = "SERVICE";
        this.category = "service";
        this.action = "service.audit";
        legacyDisabled();
        return this;
    }

    public AuditObservationBuilder change() {
        this.auditType = "CHANGE";
        this.category = "configuration";
        this.action = "change.audit";
        legacyTarget("cm.user_action_log");
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
        if (throwable != null) {
            attribute(ScmErrorAttributes.TYPE, throwable.getClass().getName());
            attribute(ScmErrorAttributes.MESSAGE, safeMessage(throwable));
        }
        return this;
    }

    public void write() {
        if (!observation.isEnabled(ObservationSignal.AUDIT)) {
            return;
        }
        Instant timestamp = observation.now();
        LinkedHashMap<String, Object> document = observation.baseDocument(
                ObservationStream.AUDIT,
                "event",
                category,
                action,
                outcome,
                correlationId,
                legacyEnabled,
                legacyTable,
                timestamp
        );
        observation.putAttribute(document, ScmAuditAttributes.TYPE, auditType);
        observation.putAttribute(document, ScmAuditAttributes.USER_NAME, userName);
        observation.putAttribute(document, ScmAuditAttributes.RESOURCE_TYPE, resourceType);
        observation.putAttribute(document, ScmAuditAttributes.RESOURCE_ID, resourceId);
        observation.putAttributes(document, attributes);
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
