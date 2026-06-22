package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationStream;

public final class ScmMetricAttributes {
    private ScmMetricAttributes() {
    }

    public static final ObservationAttributeKey<String> APP_NAME = metric("app_name", "Metric tag: application name");
    public static final ObservationAttributeKey<String> APP_PROFILE = metric("app_profile", "Metric tag: application profile");
    public static final ObservationAttributeKey<String> APP_LABEL = metric("app_label", "Metric tag: application label");
    public static final ObservationAttributeKey<String> PLATFORM = metric("platform", "Metric tag: platform");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = metric("channel_code", "Metric tag: channel code");
    public static final ObservationAttributeKey<String> GATEWAY_NAME = metric("gateway_name", "Metric tag: gateway name");
    public static final ObservationAttributeKey<String> PROTOCOL = metric("protocol", "Metric tag: gateway protocol");
    public static final ObservationAttributeKey<String> REQUEST_NAME = metric("request_name", "Metric tag: low-cardinality request name");
    public static final ObservationAttributeKey<String> SERVICE_CODE = metric("service_code", "Metric tag: service code");
    public static final ObservationAttributeKey<String> OPERATION_CODE = metric("operation_code", "Metric tag: operation code");
    public static final ObservationAttributeKey<String> PROVIDER_CODE = metric("provider_code", "Metric tag: provider code");
    public static final ObservationAttributeKey<String> PROVIDER_TYPE = metric("provider_type", "Metric tag: provider type");
    public static final ObservationAttributeKey<String> OUTCOME = metric("outcome", "Metric tag: outcome");
    public static final ObservationAttributeKey<String> ERROR_CODE = metric("error_code", "Metric tag: error code");

    private static ObservationAttributeKey<String> metric(String name, String description) {
        return ObservationAttributeKey.stringKey(name, ObservationAttributePresence.EVENT_OPTIONAL, description, ObservationStream.METRIC);
    }
}
