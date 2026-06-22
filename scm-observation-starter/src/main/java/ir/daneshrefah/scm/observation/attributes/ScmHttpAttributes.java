package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.util.List;

public final class ScmHttpAttributes {
    private ScmHttpAttributes() {
    }

    public static final ObservationAttributeKey<String> METHOD = ObservationAttributeKey.stringKey(
            "http.method", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP method", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> URL_PATH = ObservationAttributeKey.stringKey(
            "url.path", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP request path without query string", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<Integer> STATUS_CODE = ObservationAttributeKey.integerKey(
            "http.status_code", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP response status code", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> ROUTE = ObservationAttributeKey.stringKey(
            "http.route", ObservationAttributePresence.EVENT_OPTIONAL, "Low-cardinality HTTP route template", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<Boolean> QUERY_PRESENT = ObservationAttributeKey.booleanKey(
            "http.query.present", ObservationAttributePresence.EVENT_OPTIONAL, "Whether an HTTP query string was present", ObservationStream.TRACE, ObservationStream.AUDIT);

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(METHOD, URL_PATH, STATUS_CODE, ROUTE, QUERY_PRESENT);
    }
}
