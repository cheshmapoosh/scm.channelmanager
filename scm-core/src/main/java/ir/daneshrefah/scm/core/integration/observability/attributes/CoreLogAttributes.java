package ir.daneshrefah.scm.core.integration.observability.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.log.LogAttribute;

import java.util.List;

public final class CoreLogAttributes {
    private static final String OWNER = "scm-core";

    public static final ObservationAttributeKey<String> GATEWAY_NAME = keyword("scm.gateway.name", "Gateway name.");
    public static final ObservationAttributeKey<String> CHANNEL_CODE = keyword("scm.channel.code", "Channel code.");
    public static final ObservationAttributeKey<String> ROUTE_ID = keyword("scm.route.id", "Gateway route or flow identifier.");
    public static final ObservationAttributeKey<String> SERVICE_CODE = keyword("scm.service.code", "Service code.");
    public static final ObservationAttributeKey<String> OPERATION_CODE = keyword("scm.operation.code", "Operation code.");
    public static final ObservationAttributeKey<String> OPERATION_NAME = keyword("scm.operation.name", "Operation name.");
    public static final ObservationAttributeKey<String> EXCHANGE_ID = keyword("scm.exchange.id", "Camel exchange identifier.");
    public static final ObservationAttributeKey<String> PROTOCOL = keyword("scm.protocol", "Gateway protocol.");
    public static final ObservationAttributeKey<String> PLUGIN_NAME = keyword("plugin.name", "Plugin name.");
    public static final ObservationAttributeKey<String> PLUGIN_TYPE = keyword("plugin.type", "Plugin type.");
    public static final ObservationAttributeKey<String> PLUGIN_PHASE = keyword("plugin.phase", "Plugin phase.");
    public static final ObservationAttributeKey<String> PLUGIN_LAYER = keyword("plugin.layer", "Plugin layer.");
    public static final ObservationAttributeKey<Long> PLUGIN_DURATION_MS = LogAttribute.longNumber(
            "plugin.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Plugin duration in milliseconds.");

    private CoreLogAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                GATEWAY_NAME, CHANNEL_CODE, ROUTE_ID, SERVICE_CODE, OPERATION_CODE, OPERATION_NAME,
                EXCHANGE_ID, PROTOCOL, PLUGIN_NAME, PLUGIN_TYPE, PLUGIN_PHASE, PLUGIN_LAYER, PLUGIN_DURATION_MS
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return LogAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
