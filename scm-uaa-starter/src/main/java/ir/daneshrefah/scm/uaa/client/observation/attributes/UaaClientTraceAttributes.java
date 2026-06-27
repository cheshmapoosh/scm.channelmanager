package ir.daneshrefah.scm.uaa.client.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class UaaClientTraceAttributes {
    public static final ObservationAttributeKey<String> OPERATION_NAME = TraceAttribute.keyword(
            "scm.operation.name", "scm-uaa-starter", ObservationAttributePresence.EVENT_OPTIONAL,
            "UAA client operation name.");

    private UaaClientTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
