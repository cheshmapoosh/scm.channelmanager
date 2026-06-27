package ir.daneshrefah.scm.observation.attributes.metric;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

import java.util.List;

public final class CommonMetricTags {
    public static final ObservationAttributeKey<String> SERVICE_NAME = MetricTag.lowCardinality("service_name", "SCM service name.");
    public static final ObservationAttributeKey<String> ENVIRONMENT = MetricTag.lowCardinality("environment", "Deployment environment.");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = MetricTag.lowCardinality("channel_code", "Channel code.");
    public static final ObservationAttributeKey<String> OPERATION_CODE = MetricTag.lowCardinality("operation_code", "Operation code.");
    public static final ObservationAttributeKey<String> OUTCOME = MetricTag.lowCardinality("outcome", "Operation outcome.");
    public static final ObservationAttributeKey<String> ERROR_CODE = MetricTag.lowCardinality("error_code", "Bounded application error code.");
    public static final ObservationAttributeKey<String> STATUS_CODE = MetricTag.lowCardinality("status_code", "Bounded status code.");

    private CommonMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(SERVICE_NAME, ENVIRONMENT, CHANNEL_CODE, OPERATION_CODE, OUTCOME, ERROR_CODE, STATUS_CODE);
    }
}
