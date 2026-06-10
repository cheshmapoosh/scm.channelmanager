package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmMetricAttributes {
    private ScmMetricAttributes() {
    }

    public static final ObservationAttributeKey<String> APP_NAME = ObservationAttributeKey.stringKey("app_name", "Metric tag: application name");
    public static final ObservationAttributeKey<String> APP_PROFILE = ObservationAttributeKey.stringKey("app_profile", "Metric tag: application profile");
    public static final ObservationAttributeKey<String> APP_LABEL = ObservationAttributeKey.stringKey("app_label", "Metric tag: application label");
    public static final ObservationAttributeKey<String> PLATFORM = ObservationAttributeKey.stringKey("platform", "Metric tag: platform");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = ObservationAttributeKey.stringKey("channel_code", "Metric tag: channel code");
    public static final ObservationAttributeKey<String> GATEWAY_NAME = ObservationAttributeKey.stringKey("gateway_name", "Metric tag: gateway name");
    public static final ObservationAttributeKey<String> PROTOCOL = ObservationAttributeKey.stringKey("protocol", "Metric tag: gateway protocol");
    public static final ObservationAttributeKey<String> REQUEST_NAME = ObservationAttributeKey.stringKey("request_name", "Metric tag: low-cardinality request name");
    public static final ObservationAttributeKey<String> SERVICE_CODE = ObservationAttributeKey.stringKey("service_code", "Metric tag: service code");
    public static final ObservationAttributeKey<String> OPERATION_CODE = ObservationAttributeKey.stringKey("operation_code", "Metric tag: operation code");
    public static final ObservationAttributeKey<String> PROVIDER_CODE = ObservationAttributeKey.stringKey("provider_code", "Metric tag: provider code");
    public static final ObservationAttributeKey<String> PROVIDER_TYPE = ObservationAttributeKey.stringKey("provider_type", "Metric tag: provider type");
    public static final ObservationAttributeKey<String> OUTCOME = ObservationAttributeKey.stringKey("outcome", "Metric tag: outcome");
    public static final ObservationAttributeKey<String> ERROR_CODE = ObservationAttributeKey.stringKey("error_code", "Metric tag: error code");
}
