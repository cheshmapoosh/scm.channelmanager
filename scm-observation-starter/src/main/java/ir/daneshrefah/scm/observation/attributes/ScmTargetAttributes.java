package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmTargetAttributes {
    private ScmTargetAttributes() {
    }

    public static final ObservationAttributeKey<String> INDEX = ObservationAttributeKey.stringKey("scm.target.index", true, "Target index routing name");
    public static final ObservationAttributeKey<Boolean> LEGACY_ENABLED = ObservationAttributeKey.booleanKey("scm.target.legacy.enabled", true, "Whether legacy projection is enabled");
    public static final ObservationAttributeKey<String> LEGACY_TABLE = ObservationAttributeKey.stringKey("scm.target.legacy.table", "Legacy projection table");
}
