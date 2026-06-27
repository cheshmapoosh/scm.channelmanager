package ir.daneshrefah.scm.cache.client.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;

import java.util.List;

public final class CacheClientMetricTags {
    public static final ObservationAttributeKey<String> OPERATION_NAME = MetricTag.lowCardinality(
            "scm.operation.name", "scm-cache-starter", "Cache client operation name.");

    private CacheClientMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
