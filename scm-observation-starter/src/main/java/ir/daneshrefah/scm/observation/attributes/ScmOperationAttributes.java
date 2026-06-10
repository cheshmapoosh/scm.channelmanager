package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmOperationAttributes {
    private ScmOperationAttributes() {
    }

    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey("scm.operation.code", "Operation code");
    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.operation.name", "Operation name");
    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("scm.operation.type", "Operation type");
    public static final ObservationAttributeKey<Long> DURATION_MS = ObservationAttributeKey.longKey("scm.operation.duration_ms", "Operation duration in milliseconds");
}
