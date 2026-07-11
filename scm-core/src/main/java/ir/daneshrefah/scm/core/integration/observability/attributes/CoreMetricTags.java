package ir.daneshrefah.scm.core.integration.observability.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.attributes.metric.MetricTag;

import java.util.List;

public final class CoreMetricTags {
    private static final String OWNER = "scm-core";

    public static final ObservationAttributeKey<String> APP_NAME = tag("app_name", "Application name.");
    public static final ObservationAttributeKey<String> APP_PROFILE = tag("app_profile", "Application profile.");
    public static final ObservationAttributeKey<String> GATEWAY_NAME = tag("gateway_name", "Gateway name.");
    public static final ObservationAttributeKey<String> SERVICE_CODE = tag("service_code", "Service code.");

    private CoreMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(APP_NAME, APP_PROFILE, GATEWAY_NAME, SERVICE_CODE);
    }

    private static ObservationAttributeKey<String> tag(String name, String description) {
        return MetricTag.lowCardinality(name, OWNER, description);
    }
}
