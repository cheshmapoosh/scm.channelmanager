package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmClientAttributes {
    private ScmClientAttributes() {
    }

    public static final ObservationAttributeKey<String> IP = ObservationAttributeKey.stringKey("client.ip", "Client IP address");
    public static final ObservationAttributeKey<String> ADDRESS = ObservationAttributeKey.stringKey("client.address", "Protocol-neutral client or peer address");
    public static final ObservationAttributeKey<String> PHONE_NUMBER = ObservationAttributeKey.stringKey("scm.client.phone_number", "Client phone number");
    public static final ObservationAttributeKey<String> ACCESS_PARAMETER = ObservationAttributeKey.stringKey("scm.access_parameter", "Access parameter");
}
