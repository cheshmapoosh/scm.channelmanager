package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.util.List;

public final class ScmOperationAttributes {
    private ScmOperationAttributes() {
    }

    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey(
            "scm.operation.code", ObservationAttributePresence.EVENT_OPTIONAL, "Operation code", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey(
            "scm.operation.name", ObservationAttributePresence.EVENT_OPTIONAL, "Operation name", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey(
            "scm.operation.type", ObservationAttributePresence.EVENT_OPTIONAL, "Operation type", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<Long> DURATION_MS = ObservationAttributeKey.longKey(
            "scm.operation.duration_ms", ObservationAttributePresence.EVENT_OPTIONAL, "Operation duration in milliseconds", ObservationStream.TRACE, ObservationStream.AUDIT);

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(CODE, NAME, TYPE, DURATION_MS);
    }
}
