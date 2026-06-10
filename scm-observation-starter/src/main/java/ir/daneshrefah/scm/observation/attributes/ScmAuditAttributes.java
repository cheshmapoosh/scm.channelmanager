package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmAuditAttributes {
    private ScmAuditAttributes() {
    }

    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("scm.audit.type", true, "Audit type: SERVICE or CHANGE");
    public static final ObservationAttributeKey<String> USER_NAME = ObservationAttributeKey.stringKey("user.name", true, "Authenticated application principal username");
    public static final ObservationAttributeKey<String> RESOURCE_TYPE = ObservationAttributeKey.stringKey("scm.audit.resource.type", "Audited resource type");
    public static final ObservationAttributeKey<String> RESOURCE_ID = ObservationAttributeKey.stringKey("scm.audit.resource.id", "Safe audited resource identifier");
}
