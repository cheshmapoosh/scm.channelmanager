package ir.daneshrefah.scm.cache.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class CacheTraceAttributes {
    private static final String OWNER = "scm-cache";

    public static final ObservationAttributeKey<Long> OPERATION_DURATION_MS = TraceAttribute.longNumber(
            "cache.operation.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL,
            "Cache operation duration in milliseconds.");

    private CacheTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(OPERATION_DURATION_MS);
    }
}
