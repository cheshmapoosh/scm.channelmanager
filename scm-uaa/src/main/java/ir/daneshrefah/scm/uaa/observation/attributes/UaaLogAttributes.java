package ir.daneshrefah.scm.uaa.observation.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.log.LogAttribute;

import java.util.List;

public final class UaaLogAttributes {
    private static final String OWNER = "scm-uaa";

    public static final ObservationAttributeKey<String> AUTH_TYPE = keyword("uaa.auth.type", "Authentication type.");
    public static final ObservationAttributeKey<String> AUTH_CLIENT_ID = keyword("uaa.auth.client.id", "OAuth2 client identifier.");
    public static final ObservationAttributeKey<String> AUTH_GRANT_TYPE = keyword("uaa.auth.grant.type", "OAuth2 grant type.");
    public static final ObservationAttributeKey<String> AUTH_METHOD = keyword("uaa.auth.method", "Authentication method.");
    public static final ObservationAttributeKey<String> AUTH_STEP = keyword("uaa.auth.step", "Authentication step.");
    public static final ObservationAttributeKey<String> AUTH_RESULT = keyword("uaa.auth.result", "Authentication result.");
    public static final ObservationAttributeKey<String> AUTH_FAILURE_REASON = keyword("uaa.auth.failure.reason", "Safe authentication failure reason.");
    public static final ObservationAttributeKey<String> OTP_CHANNEL = keyword("uaa.otp.channel", "OTP channel.");
    public static final ObservationAttributeKey<String> OTP_PURPOSE = keyword("uaa.otp.purpose", "OTP purpose.");
    public static final ObservationAttributeKey<String> TOKEN_TYPE = keyword("uaa.token.type", "Token type.");
    public static final ObservationAttributeKey<Boolean> JWT_PRESENT = LogAttribute.booleanValue(
            "uaa.jwt.present", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Whether a JWT was present.");
    public static final ObservationAttributeKey<String> JWT_ISSUER = keyword("uaa.jwt.issuer", "JWT issuer.");
    public static final ObservationAttributeKey<String> JWT_SUBJECT = keyword("uaa.jwt.subject", "JWT subject.");
    public static final ObservationAttributeKey<String> JWT_USERNAME = keyword("uaa.jwt.username", "JWT username.");
    public static final ObservationAttributeKey<String> JWT_MASKED = LogAttribute.maskedKeyword(
            "uaa.jwt.masked", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, 5, 5,
            "Masked JWT value; never the raw token.");
    public static final ObservationAttributeKey<String> JWT_EXPIRATION = keyword("uaa.jwt.expiration", "JWT expiration.");
    public static final ObservationAttributeKey<String> DB_DATASOURCE = keyword("uaa.db.datasource", "UAA datasource name.");
    public static final ObservationAttributeKey<String> MESSAGING_SYSTEM = keyword("uaa.messaging.system", "Messaging system.");
    public static final ObservationAttributeKey<String> MESSAGING_DESTINATION_NAME = keyword(
            "uaa.messaging.destination.name", "Messaging destination name.");
    public static final ObservationAttributeKey<String> CLIENT_IP = keyword("uaa.client.ip", "UAA client IP address.");
    public static final ObservationAttributeKey<String> HTTP_METHOD = keyword("uaa.http.method", "UAA HTTP method.");
    public static final ObservationAttributeKey<String> URL_PATH = keyword("uaa.url.path", "UAA request path without query string.");
    public static final ObservationAttributeKey<Integer> HTTP_STATUS_CODE = LogAttribute.integer(
            "uaa.http.status_code", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "UAA HTTP response status code.");

    private UaaLogAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                AUTH_TYPE, AUTH_CLIENT_ID, AUTH_GRANT_TYPE, AUTH_METHOD, AUTH_STEP, AUTH_RESULT, AUTH_FAILURE_REASON,
                OTP_CHANNEL, OTP_PURPOSE, TOKEN_TYPE, JWT_PRESENT, JWT_ISSUER, JWT_SUBJECT, JWT_USERNAME,
                JWT_MASKED, JWT_EXPIRATION, DB_DATASOURCE, MESSAGING_SYSTEM, MESSAGING_DESTINATION_NAME,
                CLIENT_IP, HTTP_METHOD, URL_PATH, HTTP_STATUS_CODE
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return LogAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
