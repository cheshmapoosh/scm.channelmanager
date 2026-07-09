package ir.daneshrefah.scm.cmconnector.observation.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.attributes.metric.MetricTag;

import java.util.List;

public final class CmConnectorMetricTags {
    private static final String OWNER = "scm-cm-connector";

    public static final ObservationAttributeKey<String> OPERATION_NAME = MetricTag.lowCardinality(
            "scm.operation.name", OWNER, "CM connector operation name.");

    private CmConnectorMetricTags() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
