package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmProviderAttributes {
    private ScmProviderAttributes() {
    }

    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey("scm.provider.code", ObservationAttributePresence.EVENT_OPTIONAL, "Provider code");
    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.provider.name", ObservationAttributePresence.EVENT_OPTIONAL, "Provider name");
    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("scm.provider.type", ObservationAttributePresence.EVENT_OPTIONAL, "Provider type");
    public static final ObservationAttributeKey<String> STATUS = ObservationAttributeKey.stringKey("scm.provider.status", ObservationAttributePresence.EVENT_OPTIONAL, "Provider status");
    public static final ObservationAttributeKey<String> RESPONSE_CODE = ObservationAttributeKey.stringKey("scm.provider.response_code", ObservationAttributePresence.EVENT_OPTIONAL, "Provider response code");
    public static final ObservationAttributeKey<Long> DURATION_MS = ObservationAttributeKey.longKey("scm.provider.duration_ms", ObservationAttributePresence.EVENT_OPTIONAL, "Provider execution duration in milliseconds");
    public static final ObservationAttributeKey<String> ERROR_CODE = ObservationAttributeKey.stringKey("scm.provider.error_code", ObservationAttributePresence.ERROR_OPTIONAL, "Provider error code");
    public static final ObservationAttributeKey<String> ERROR_MESSAGE = ObservationAttributeKey.stringKey("scm.provider.error_message", ObservationAttributePresence.ERROR_OPTIONAL, "Provider error message");
}
