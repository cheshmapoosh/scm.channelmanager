package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmAuthAttributes {
    private ScmAuthAttributes() {
    }

    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("scm.auth.type", "Authentication type");
    public static final ObservationAttributeKey<Boolean> JWT_PRESENT = ObservationAttributeKey.booleanKey("scm.auth.jwt.present", "Whether a JWT was present");
    public static final ObservationAttributeKey<String> JWT_ISSUER = ObservationAttributeKey.stringKey("scm.auth.jwt.issuer", "JWT issuer");
    public static final ObservationAttributeKey<String> JWT_SUBJECT = ObservationAttributeKey.stringKey("scm.auth.jwt.subject", "JWT subject");
    public static final ObservationAttributeKey<String> JWT_USERNAME = ObservationAttributeKey.stringKey("scm.auth.jwt.username", "JWT username");
    public static final ObservationAttributeKey<String> JWT_EXP = ObservationAttributeKey.stringKey("scm.auth.jwt.exp", "JWT expiration");
    public static final ObservationAttributeKey<String> JWT_HASH = ObservationAttributeKey.stringKey("scm.auth.jwt.hash", "JWT hash; raw token is forbidden");
}
