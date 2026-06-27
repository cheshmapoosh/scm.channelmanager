package ir.daneshrefah.scm.cache.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;

import java.util.List;

public final class CacheMetricTags {
    private static final String OWNER = "scm-cache";

    public static final ObservationAttributeKey<String> HTTP_METHOD = commonTag("http.method", "HTTP method.");
    public static final ObservationAttributeKey<String> HTTP_ROUTE = commonTag("http.route", "HTTP route template.");
    public static final ObservationAttributeKey<String> OPERATION_TYPE = commonTag("scm.operation.type", "Cache operation type.");
    public static final ObservationAttributeKey<String> HTTP_STATUS_CODE = commonTag("http.status_code", "HTTP status code.");

    private CacheMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(HTTP_METHOD, HTTP_ROUTE, OPERATION_TYPE, HTTP_STATUS_CODE);
    }

    private static ObservationAttributeKey<String> tag(String name, String description) {
        return MetricTag.lowCardinality(name, OWNER, description);
    }

    private static ObservationAttributeKey<String> commonTag(String name, String description) {
        return MetricTag.lowCardinality(name, description);
    }
}
