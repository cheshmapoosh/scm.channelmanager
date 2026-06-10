package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmServiceAttributes {
    private ScmServiceAttributes() {
    }

    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey("scm.service.code", "Service code");
    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.service.name", "Service name");
    public static final ObservationAttributeKey<String> VERSION = ObservationAttributeKey.stringKey("scm.service.version", "Service version");
}
