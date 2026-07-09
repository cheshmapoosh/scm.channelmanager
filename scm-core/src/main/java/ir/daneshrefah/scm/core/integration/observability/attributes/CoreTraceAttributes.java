package ir.daneshrefah.scm.core.integration.observability.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.attributes.trace.TraceAttribute;

import java.util.List;

public final class CoreTraceAttributes {
    private static final String OWNER = "scm-core";

    public static final ObservationAttributeKey<String> SERVICE_CODE = keyword("scm.service.code", "Service code.");
    public static final ObservationAttributeKey<String> SERVICE_NAME = keyword("scm.service.name", "Service name.");
    public static final ObservationAttributeKey<String> SERVICE_VERSION = keyword("scm.service.version", "Service version.");
    public static final ObservationAttributeKey<String> OPERATION_CODE = keyword("scm.operation.code", "Operation code.");
    public static final ObservationAttributeKey<String> OPERATION_NAME = keyword("scm.operation.name", "Operation name.");
    public static final ObservationAttributeKey<String> OPERATION_TYPE = keyword("scm.operation.type", "Operation type.");
    public static final ObservationAttributeKey<Long> OPERATION_DURATION_MS = TraceAttribute.longNumber(
            "scm.operation.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Operation duration in milliseconds.");
    public static final ObservationAttributeKey<Long> SERVICE_DURATION_MS = TraceAttribute.longNumber(
            "scm.service.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Service duration in milliseconds.");
    public static final ObservationAttributeKey<String> EXCHANGE_ID = keyword("scm.exchange.id", "Camel exchange identifier.");
    public static final ObservationAttributeKey<String> TARGET_KIND = keyword("scm.runtime.target.kind", "Runtime target kind.");
    public static final ObservationAttributeKey<String> PLUGIN_NAME = keyword("plugin.name", "Plugin name.");
    public static final ObservationAttributeKey<String> PLUGIN_TYPE = keyword("plugin.type", "Plugin type.");
    public static final ObservationAttributeKey<String> PLUGIN_PHASE = keyword("plugin.phase", "Plugin phase.");
    public static final ObservationAttributeKey<String> PLUGIN_LAYER = keyword("plugin.layer", "Plugin layer.");
    public static final ObservationAttributeKey<Long> PLUGIN_DURATION_MS = TraceAttribute.longNumber(
            "plugin.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL, "Plugin duration in milliseconds.");

    private CoreTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                SERVICE_CODE, SERVICE_NAME, SERVICE_VERSION,
                OPERATION_CODE, OPERATION_NAME, OPERATION_TYPE, OPERATION_DURATION_MS, SERVICE_DURATION_MS,
                EXCHANGE_ID, TARGET_KIND,
                PLUGIN_NAME, PLUGIN_TYPE, PLUGIN_PHASE, PLUGIN_LAYER, PLUGIN_DURATION_MS
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
