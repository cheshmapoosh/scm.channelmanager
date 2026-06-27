package ir.daneshrefah.scm.cmconnector.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.attributes.metric.MetricTag;

import java.util.List;

public final class CmConnectorMetricTags {
    public static final ObservationAttributeKey<String> OPERATION_NAME = MetricTag.lowCardinality(
            "scm.operation.name", "scm-cm-connector", "CM connector operation name.");

    private CmConnectorMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
