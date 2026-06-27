package ir.daneshrefah.scm.web.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class WebTraceAttributes {
    private static final String OWNER = "scm-web";

    public static final ObservationAttributeKey<String> HTTP_METHOD = keyword("http.method", "HTTP method.");
    public static final ObservationAttributeKey<String> URL_PATH = keyword("url.path", "HTTP request path without query string.");
    public static final ObservationAttributeKey<Integer> HTTP_STATUS_CODE = TraceAttribute.integerNumber(
            "http.status_code", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "HTTP response status code.");
    public static final ObservationAttributeKey<String> HTTP_ROUTE = keyword("http.route", "Low-cardinality HTTP route template.");
    public static final ObservationAttributeKey<Boolean> QUERY_PRESENT = TraceAttribute.booleanValue(
            "http.query.present", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Whether an HTTP query string was present.");
    public static final ObservationAttributeKey<String> CLIENT_IP = keyword("client.ip", "Client IP address.");
    public static final ObservationAttributeKey<String> CLIENT_ADDRESS = keyword("client.address", "Protocol-neutral client address.");
    public static final ObservationAttributeKey<String> GATEWAY_REQUEST_NAME = keyword("scm.request.name", "Low-cardinality gateway request name.");
    public static final ObservationAttributeKey<String> MESSAGE_ID = keyword("scm.message.id", "Protocol message identifier.");

    private WebTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                HTTP_METHOD, URL_PATH, HTTP_STATUS_CODE, HTTP_ROUTE, QUERY_PRESENT,
                CLIENT_IP, CLIENT_ADDRESS, GATEWAY_REQUEST_NAME, MESSAGE_ID
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
