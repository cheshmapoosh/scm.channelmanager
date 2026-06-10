package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmErrorAttributes {
    private ScmErrorAttributes() {
    }

    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("error.type", "Error type");
    public static final ObservationAttributeKey<String> MESSAGE = ObservationAttributeKey.stringKey("error.message", "Safe error message");
    public static final ObservationAttributeKey<String> STACK_TRACE = ObservationAttributeKey.stringKey("error.stack_trace", "Error stack trace");
    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey("error.code", "Error code");
}
