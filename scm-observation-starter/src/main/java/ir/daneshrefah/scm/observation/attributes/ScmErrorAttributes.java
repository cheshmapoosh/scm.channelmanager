package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationStream;

import java.util.List;

public final class ScmErrorAttributes {
    private ScmErrorAttributes() {
    }

    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey(
            "error.type", ObservationAttributePresence.ERROR_REQUIRED, "Error type", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> MESSAGE = ObservationAttributeKey.stringKey(
            "error.message", ObservationAttributePresence.ERROR_OPTIONAL, "Safe error message", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> STACK_TRACE = ObservationAttributeKey.stringKey(
            "error.stack_trace", ObservationAttributePresence.ERROR_OPTIONAL, "Error stack trace", ObservationStream.TRACE, ObservationStream.AUDIT);
    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey(
            "error.code", ObservationAttributePresence.ERROR_OPTIONAL, "Error code", ObservationStream.TRACE, ObservationStream.AUDIT);

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(TYPE, MESSAGE, STACK_TRACE, CODE);
    }
}
