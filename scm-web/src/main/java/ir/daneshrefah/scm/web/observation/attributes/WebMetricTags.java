package ir.daneshrefah.scm.web.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;

import java.util.List;

public final class WebMetricTags {
    private static final String OWNER = "scm-web";

    public static final ObservationAttributeKey<String> PROTOCOL = tag("protocol", "Gateway protocol.");
    public static final ObservationAttributeKey<String> REQUEST_NAME = tag("request_name", "Gateway request name.");
    public static final ObservationAttributeKey<String> HTTP_METHOD = tag("http.method", "HTTP method.");
    public static final ObservationAttributeKey<String> HTTP_ROUTE = tag("http.route", "HTTP route template.");
    public static final ObservationAttributeKey<String> OPERATION_TYPE = tag("scm.operation.type", "Operation type.");
    public static final ObservationAttributeKey<String> HTTP_STATUS_CODE = tag("http.status_code", "HTTP status code.");

    private WebMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(PROTOCOL, REQUEST_NAME, HTTP_METHOD, HTTP_ROUTE, OPERATION_TYPE, HTTP_STATUS_CODE);
    }

    private static ObservationAttributeKey<String> tag(String name, String description) {
        return MetricTag.lowCardinality(name, OWNER, description);
    }
}
