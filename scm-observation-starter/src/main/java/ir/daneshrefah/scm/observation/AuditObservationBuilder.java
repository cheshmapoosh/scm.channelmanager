package ir.daneshrefah.scm.observation;

import ir.daneshrefah.scm.observation.attributes.ScmAuditAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmErrorAttributes;
import ir.daneshrefah.scm.observation.attributes.ScmObservationDocumentAttributes;
import ir.daneshrefah.scm.observation.policy.ObservationSignal;

import java.time.Instant;
import java.util.LinkedHashMap;

public class AuditObservationBuilder extends AbstractObservationBuilder<AuditObservationBuilder> {
    private String auditType = "SERVICE";
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
        this.auditType = "SERVICE";
        this.category = "service";
        this.action = "service.audit";
        this.recordKind = ObservationRecordKind.EVENT;
        return this;
    }

    public AuditObservationBuilder change() {
        this.auditType = "CHANGE";
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
        ObservationDocumentBuilder builder = observation.documentFactory().audit(
                recordKind,
                throwable != null,
                timestamp,
                action,
                correlationId,
                "operation"
        );
        builder.put(ScmObservationDocumentAttributes.EVENT_CATEGORY, category);
        builder.put(ScmObservationDocumentAttributes.EVENT_ACTION, action);
        builder.put(ScmObservationDocumentAttributes.EVENT_OUTCOME, outcome);
        builder.put(ScmAuditAttributes.TYPE, auditType);
        builder.put(ScmAuditAttributes.USER_NAME, userName);
        builder.put(ScmAuditAttributes.RESOURCE_TYPE, resourceType);
        builder.put(ScmAuditAttributes.RESOURCE_ID, resourceId);
        builder.putAll(attributes);
        LinkedHashMap<String, Object> document = builder.build();
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
