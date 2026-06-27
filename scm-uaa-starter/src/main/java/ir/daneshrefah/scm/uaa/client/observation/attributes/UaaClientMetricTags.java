package ir.daneshrefah.scm.uaa.client.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;

import java.util.List;

public final class UaaClientMetricTags {
    public static final ObservationAttributeKey<String> OPERATION_NAME = MetricTag.lowCardinality(
            "scm.operation.name", "UAA client operation name.");

    private UaaClientMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
