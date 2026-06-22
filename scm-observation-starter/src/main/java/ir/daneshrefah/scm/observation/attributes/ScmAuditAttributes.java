package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.util.List;

public final class ScmAuditAttributes {
    private ScmAuditAttributes() {
    }

    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey(
            "scm.audit.type", ObservationAttributePresence.ALWAYS_REQUIRED, "Audit type: SERVICE or CHANGE", ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> USER_NAME = ObservationAttributeKey.stringKey(
            "user.name", ObservationAttributePresence.EVENT_OPTIONAL, "Authenticated application principal username", ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> RESOURCE_TYPE = ObservationAttributeKey.stringKey(
            "scm.audit.resource.type", ObservationAttributePresence.EVENT_OPTIONAL, "Audited resource type", ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> RESOURCE_ID = ObservationAttributeKey.stringKey(
            "scm.audit.resource.id", ObservationAttributePresence.EVENT_OPTIONAL, "Safe audited resource identifier", ObservationStream.AUDIT);

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(TYPE, USER_NAME, RESOURCE_TYPE, RESOURCE_ID);
    }
}
