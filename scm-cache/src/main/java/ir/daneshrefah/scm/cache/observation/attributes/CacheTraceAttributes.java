package ir.daneshrefah.scm.cache.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class CacheTraceAttributes {
    private static final String OWNER = "scm-cache";

    public static final ObservationAttributeKey<String> HTTP_METHOD = keyword("http.method", "HTTP method.");
    public static final ObservationAttributeKey<String> HTTP_ROUTE = keyword("http.route", "HTTP route template.");
    public static final ObservationAttributeKey<String> URL_PATH = keyword("url.path", "HTTP URL path.");
    public static final ObservationAttributeKey<Boolean> QUERY_PRESENT = TraceAttribute.booleanValue(
            "http.query.present", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Whether a query string exists.");
    public static final ObservationAttributeKey<Integer> HTTP_STATUS_CODE = TraceAttribute.integerNumber(
            "http.status_code", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "HTTP response status code.");
    public static final ObservationAttributeKey<Long> OPERATION_DURATION_MS = TraceAttribute.longNumber(
            "scm.operation.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL,
            "Cache operation duration in milliseconds.");

    private CacheTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(HTTP_METHOD, HTTP_ROUTE, URL_PATH, QUERY_PRESENT, HTTP_STATUS_CODE, OPERATION_DURATION_MS);
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
