package ir.daneshrefah.scm.web.observation.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.trace.TraceAttribute;

import java.util.List;

public final class WebTraceAttributes {
    private static final String OWNER = "scm-web";

    public static final ObservationAttributeKey<String> USER_NICKNAME = keyword(
            "scm.user.nickname", "Validated JWT subject for gateway trace enrichment.");
    public static final ObservationAttributeKey<List<String>> JWT_SCOPE = keywordCollection(
            "scm.jwt.scope", "Normalized scopes from the validated JWT scope claim.");
    public static final ObservationAttributeKey<String> JWT_ISSUER = keyword(
            "scm.jwt.issuer", "Issuer from the validated JWT.");
//    public static final ObservationAttributeKey<String> CLIENT_ADDRESS = keyword(
//            "scm.client.address", "Client address asserted by the validated JWT acp claim.");
    public static final ObservationAttributeKey<String> JWT_ISSUE_AT = date(
            "scm.jwt.issue_at", "UTC issue time from the validated JWT.");
    public static final ObservationAttributeKey<String> JWT_EXPIRE_AT = date(
            "scm.jwt.expire_at", "UTC expiration time from the validated JWT.");
    public static final ObservationAttributeKey<List<String>> JWT_AUDIENCE = keywordCollection(
            "scm.jwt.audience", "Normalized audiences from the validated JWT.");
    public static final ObservationAttributeKey<String> JWT_GENERATOR = keyword(
            "scm.jwt.generator", "Generator from the validated JWT grn claim.");
    public static final ObservationAttributeKey<String> AUTH_TRANSACTION_METHOD = keyword(
            "scm.auth.txn_method", "Transaction authentication method from the validated JWT.");
//    public static final ObservationAttributeKey<String> AUTH_LOGIN_METHOD = keyword(
//            "scm.auth.login_method", "Login authentication method from the validated JWT.");

    private WebTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                USER_NICKNAME,
                JWT_SCOPE,
                JWT_ISSUER,
//                CLIENT_ADDRESS,
                JWT_ISSUE_AT,
                JWT_EXPIRE_AT,
                JWT_AUDIENCE,
                JWT_GENERATOR,
                AUTH_TRANSACTION_METHOD
//                AUTH_LOGIN_METHOD
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }

    private static ObservationAttributeKey<List<String>> keywordCollection(String name, String description) {
        return TraceAttribute.keywordCollection(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }

    private static ObservationAttributeKey<String> date(String name, String description) {
        return TraceAttribute.date(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
