package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmGatewayAttributes {
    private ScmGatewayAttributes() {
    }

    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.gateway.name", ObservationAttributePresence.EVENT_OPTIONAL, "Gateway name");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = ObservationAttributeKey.stringKey("scm.channel.code", ObservationAttributePresence.EVENT_OPTIONAL, "Channel code");
    public static final ObservationAttributeKey<String> PROTOCOL = ObservationAttributeKey.stringKey("scm.protocol", ObservationAttributePresence.EVENT_OPTIONAL, "Gateway protocol");
    public static final ObservationAttributeKey<String> REQUEST_NAME = ObservationAttributeKey.stringKey("scm.request.name", ObservationAttributePresence.EVENT_OPTIONAL, "Low-cardinality gateway request name");
    public static final ObservationAttributeKey<String> MESSAGE_ID = ObservationAttributeKey.stringKey("scm.message.id", ObservationAttributePresence.EVENT_OPTIONAL, "Protocol message identifier");
    public static final ObservationAttributeKey<String> ROUTE_ID = ObservationAttributeKey.stringKey("scm.route.id", ObservationAttributePresence.EVENT_OPTIONAL, "Gateway route or flow identifier");
}
