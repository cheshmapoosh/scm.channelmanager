package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmServiceAttributes {
    private ScmServiceAttributes() {
    }

    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey("scm.service.code", ObservationAttributePresence.EVENT_OPTIONAL, "Service code");
    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.service.name", ObservationAttributePresence.EVENT_OPTIONAL, "Service name");
    public static final ObservationAttributeKey<String> VERSION = ObservationAttributeKey.stringKey("scm.service.version", ObservationAttributePresence.EVENT_OPTIONAL, "Service version");
}
