package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;

public final class ScmGatewayAttributes {
    private ScmGatewayAttributes() {
    }

    public static final ObservationAttributeKey<String> NAME = ObservationAttributeKey.stringKey("scm.gateway.name", "Gateway name");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = ObservationAttributeKey.stringKey("scm.channel.code", "Channel code");
    public static final ObservationAttributeKey<String> PROTOCOL = ObservationAttributeKey.stringKey("scm.protocol", "Gateway protocol");
    public static final ObservationAttributeKey<String> REQUEST_NAME = ObservationAttributeKey.stringKey("scm.request.name", "Low-cardinality gateway request name");
    public static final ObservationAttributeKey<String> MESSAGE_ID = ObservationAttributeKey.stringKey("scm.message.id", "Protocol message identifier");
    public static final ObservationAttributeKey<String> ROUTE_ID = ObservationAttributeKey.stringKey("scm.route.id", "Gateway route or flow identifier");
}
