package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmAuthAttributes {
    private ScmAuthAttributes() {
    }

    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("scm.auth.type", ObservationAttributePresence.EVENT_OPTIONAL, "Authentication type");
    public static final ObservationAttributeKey<Boolean> JWT_PRESENT = ObservationAttributeKey.booleanKey("scm.auth.jwt.present", ObservationAttributePresence.EVENT_OPTIONAL, "Whether a JWT was present");
    public static final ObservationAttributeKey<String> JWT_ISSUER = ObservationAttributeKey.stringKey("scm.auth.jwt.issuer", ObservationAttributePresence.EVENT_OPTIONAL, "JWT issuer");
    public static final ObservationAttributeKey<String> JWT_SUBJECT = ObservationAttributeKey.stringKey("scm.auth.jwt.subject", ObservationAttributePresence.EVENT_OPTIONAL, "JWT subject");
    public static final ObservationAttributeKey<String> JWT_USERNAME = ObservationAttributeKey.stringKey("scm.auth.jwt.username", ObservationAttributePresence.EVENT_OPTIONAL, "JWT username");
    public static final ObservationAttributeKey<String> JWT_EXP = ObservationAttributeKey.stringKey("scm.auth.jwt.exp", ObservationAttributePresence.EVENT_OPTIONAL, "JWT expiration");
    public static final ObservationAttributeKey<String> JWT_HASH = ObservationAttributeKey.stringKey("scm.auth.jwt.hash", ObservationAttributePresence.EVENT_OPTIONAL, "JWT hash; raw token is forbidden");
}
