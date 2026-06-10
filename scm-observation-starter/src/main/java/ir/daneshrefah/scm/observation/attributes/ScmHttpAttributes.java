package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmHttpAttributes {
    private ScmHttpAttributes() {
    }

    public static final ObservationAttributeKey<String> METHOD = ObservationAttributeKey.stringKey("http.method", "HTTP method");
    public static final ObservationAttributeKey<String> URL_PATH = ObservationAttributeKey.stringKey("url.path", "HTTP request path without query string");
    public static final ObservationAttributeKey<Integer> STATUS_CODE = ObservationAttributeKey.integerKey("http.status_code", "HTTP response status code");
    public static final ObservationAttributeKey<String> ROUTE = ObservationAttributeKey.stringKey("http.route", "Low-cardinality HTTP route template");
    public static final ObservationAttributeKey<Boolean> QUERY_PRESENT = ObservationAttributeKey.booleanKey("http.query.present", "Whether an HTTP query string was present");
}
