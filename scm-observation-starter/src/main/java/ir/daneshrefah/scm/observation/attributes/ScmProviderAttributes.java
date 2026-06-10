package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmProviderAttributes {
    private ScmProviderAttributes() {
    }

    public static final ObservationAttributeKey<String> CODE = ObservationAttributeKey.stringKey("scm.provider.code", "Provider code");
    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.provider.name", "Provider name");
    public static final ObservationAttributeKey<String> TYPE = ObservationAttributeKey.stringKey("scm.provider.type", "Provider type");
    public static final ObservationAttributeKey<String> STATUS = ObservationAttributeKey.stringKey("scm.provider.status", "Provider status");
    public static final ObservationAttributeKey<String> RESPONSE_CODE = ObservationAttributeKey.stringKey("scm.provider.response_code", "Provider response code");
    public static final ObservationAttributeKey<Long> DURATION_MS = ObservationAttributeKey.longKey("scm.provider.duration_ms", "Provider execution duration in milliseconds");
    public static final ObservationAttributeKey<String> ERROR_CODE = ObservationAttributeKey.stringKey("scm.provider.error_code", "Provider error code");
    public static final ObservationAttributeKey<String> ERROR_MESSAGE = ObservationAttributeKey.stringKey("scm.provider.error_message", "Provider error message");
}
