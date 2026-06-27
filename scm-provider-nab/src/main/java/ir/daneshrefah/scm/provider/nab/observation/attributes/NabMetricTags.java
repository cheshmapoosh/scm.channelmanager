package ir.daneshrefah.scm.provider.nab.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;

import java.util.List;

public final class NabMetricTags {
    private static final String OWNER = "scm-provider-nab";

    public static final ObservationAttributeKey<String> PROVIDER_CODE = tag("provider_code", "Provider code.");
    public static final ObservationAttributeKey<String> PROVIDER_TYPE = tag("provider_type", "Provider type.");

    private NabMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(PROVIDER_CODE, PROVIDER_TYPE);
    }

    private static ObservationAttributeKey<String> tag(String name, String description) {
        return MetricTag.lowCardinality(name, OWNER, description);
    }
}
