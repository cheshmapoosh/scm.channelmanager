package ir.daneshrefah.scm.observation.starter.attributes.metric;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;

import java.util.List;

public final class CommonMetricTags {
    public static final ObservationAttributeKey<String> SERVICE_NAME = MetricTag.lowCardinality("service_name", "SCM service name.");
    public static final ObservationAttributeKey<String> ENVIRONMENT = MetricTag.lowCardinality("environment", "Deployment environment.");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = MetricTag.lowCardinality("channel_code", "Channel code.");
    public static final ObservationAttributeKey<String> OPERATION_CODE = MetricTag.lowCardinality("operation_code", "Operation code.");
    public static final ObservationAttributeKey<String> OUTCOME = MetricTag.lowCardinality("outcome", "Operation outcome.");
    public static final ObservationAttributeKey<String> ERROR_CODE = MetricTag.lowCardinality("error_code", "Bounded application error code.");
    public static final ObservationAttributeKey<String> STATUS_CODE = MetricTag.lowCardinality("status_code", "Bounded status code.");
    public static final ObservationAttributeKey<String> HTTP_METHOD = MetricTag.lowCardinality("http.method", "HTTP method.");
    public static final ObservationAttributeKey<String> HTTP_ROUTE = MetricTag.lowCardinality("http.route", "HTTP route template.");
    public static final ObservationAttributeKey<String> HTTP_STATUS_CODE = MetricTag.lowCardinality("http.status_code", "HTTP status code.");
    public static final ObservationAttributeKey<String> OPERATION_TYPE = MetricTag.lowCardinality("scm.operation.type", "Operation type.");
    public static final ObservationAttributeKey<String> PROTOCOL = MetricTag.lowCardinality("protocol", "Gateway or transport protocol.");
    public static final ObservationAttributeKey<String> REQUEST_NAME = MetricTag.lowCardinality("request_name", "Low-cardinality request name.");

    private CommonMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                SERVICE_NAME, ENVIRONMENT, CHANNEL_CODE, OPERATION_CODE, OUTCOME, ERROR_CODE, STATUS_CODE,
                HTTP_METHOD, HTTP_ROUTE, HTTP_STATUS_CODE, OPERATION_TYPE, PROTOCOL, REQUEST_NAME
        );
    }
}
