package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmClientAttributes {
    private ScmClientAttributes() {
    }

    public static final ObservationAttributeKey<String> IP = ObservationAttributeKey.stringKey("client.ip", ObservationAttributePresence.EVENT_OPTIONAL, "Client IP address");
    public static final ObservationAttributeKey<String> ADDRESS = ObservationAttributeKey.stringKey("client.address", ObservationAttributePresence.EVENT_OPTIONAL, "Protocol-neutral client or peer address");
    public static final ObservationAttributeKey<String> PHONE_NUMBER = ObservationAttributeKey.stringKey("scm.client.phone_number", ObservationAttributePresence.EVENT_OPTIONAL, "Client phone number");
    public static final ObservationAttributeKey<String> ACCESS_PARAMETER = ObservationAttributeKey.stringKey("scm.access_parameter", ObservationAttributePresence.EVENT_OPTIONAL, "Access parameter");
}
