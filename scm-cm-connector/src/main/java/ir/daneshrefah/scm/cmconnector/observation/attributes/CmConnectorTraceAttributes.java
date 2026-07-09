package ir.daneshrefah.scm.cmconnector.observation.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.trace.TraceAttribute;

import java.util.List;

public final class CmConnectorTraceAttributes {
    public static final ObservationAttributeKey<String> OPERATION_NAME = TraceAttribute.keyword(
            "scm.operation.name", ObservationAttributePresence.EVENT_OPTIONAL,
            "CM connector operation name.");

    private CmConnectorTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
