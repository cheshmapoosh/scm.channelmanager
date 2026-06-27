package ir.daneshrefah.scm.uaa.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class UaaTraceAttributes {
    private static final String OWNER = "scm-uaa";

    public static final ObservationAttributeKey<String> AUTH_TYPE = keyword("scm.auth.type", "Authentication type.");
    public static final ObservationAttributeKey<Boolean> JWT_PRESENT = TraceAttribute.booleanValue(
            "scm.auth.jwt.present", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Whether a JWT was issued.");
    public static final ObservationAttributeKey<String> JWT_ISSUER = keyword("scm.auth.jwt.issuer", "JWT issuer.");
    public static final ObservationAttributeKey<String> JWT_SUBJECT = masked("scm.auth.jwt.subject", "Masked JWT subject.");
    public static final ObservationAttributeKey<String> JWT_USERNAME = masked("scm.auth.jwt.username", "Masked JWT username.");
    public static final ObservationAttributeKey<String> JWT_EXPIRATION = keyword("scm.auth.jwt.exp", "JWT expiration.");
    public static final ObservationAttributeKey<String> JWT_HASH = keyword("scm.auth.jwt.hash", "One-way JWT hash; never the raw token.");
    public static final ObservationAttributeKey<String> CLIENT_IP = keyword("client.ip", "Client IP address.");
    public static final ObservationAttributeKey<String> AUTH_CLIENT_TYPE = keyword("scm.auth.client_type", "Authentication client type.");
    public static final ObservationAttributeKey<String> HTTP_METHOD = keyword("http.method", "HTTP method.");
    public static final ObservationAttributeKey<String> URL_PATH = keyword("url.path", "HTTP request path without query string.");
    public static final ObservationAttributeKey<Integer> HTTP_STATUS_CODE = TraceAttribute.integerNumber(
            "http.status_code", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "HTTP response status code.");

    private UaaTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                AUTH_TYPE, JWT_PRESENT, JWT_ISSUER, JWT_SUBJECT, JWT_USERNAME, JWT_EXPIRATION, JWT_HASH,
                CLIENT_IP, AUTH_CLIENT_TYPE, HTTP_METHOD, URL_PATH, HTTP_STATUS_CODE
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }

    private static ObservationAttributeKey<String> masked(String name, String description) {
        return TraceAttribute.maskedKeyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, 0, 0, description);
    }
}
