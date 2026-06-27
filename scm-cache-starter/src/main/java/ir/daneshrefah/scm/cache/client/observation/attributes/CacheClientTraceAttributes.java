package ir.daneshrefah.scm.cache.client.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class CacheClientTraceAttributes {
    public static final ObservationAttributeKey<String> OPERATION_NAME = TraceAttribute.keyword(
            "scm.operation.name", ObservationAttributePresence.EVENT_OPTIONAL,
            "Cache client operation name.");

    private CacheClientTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_NAME);
    }
}
