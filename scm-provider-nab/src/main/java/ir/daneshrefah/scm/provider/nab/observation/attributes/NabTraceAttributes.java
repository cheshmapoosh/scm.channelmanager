package ir.daneshrefah.scm.provider.nab.observation.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.trace.TraceAttribute;

import java.util.List;

public final class NabTraceAttributes {
    private static final String OWNER = "scm-provider-nab";

    public static final ObservationAttributeKey<String> PROVIDER_CODE = keyword("scm.provider.code", "Provider code.");
    public static final ObservationAttributeKey<String> PROVIDER_NAME = keyword("scm.provider.name", "Provider name.");
    public static final ObservationAttributeKey<String> PROVIDER_TYPE = keyword("scm.provider.type", "Provider type.");
    public static final ObservationAttributeKey<String> PROVIDER_STATUS = keyword("scm.provider.status", "Provider status.");
    public static final ObservationAttributeKey<String> PROVIDER_RESPONSE_CODE = keyword("scm.provider.response_code", "Provider response code.");
    public static final ObservationAttributeKey<Long> PROVIDER_DURATION_MS = TraceAttribute.longNumber(
            "scm.provider.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL,
            "Provider execution duration in milliseconds.");
    public static final ObservationAttributeKey<String> PROVIDER_ERROR_CODE = TraceAttribute.keyword(
            "scm.provider.error_code", OWNER, ObservationAttributePresence.ERROR_OPTIONAL, "Provider error code.");
    public static final ObservationAttributeKey<String> PROVIDER_ERROR_MESSAGE = TraceAttribute.text(
            "scm.provider.error_message", OWNER, ObservationAttributePresence.ERROR_OPTIONAL, "Safe provider error message.");
    public static final ObservationAttributeKey<String> OPERATION_CODE = TraceAttribute.keyword(
            "scm.operation.code", ObservationAttributePresence.EVENT_OPTIONAL, "Operation code.");
    public static final ObservationAttributeKey<String> OPERATION_NAME = TraceAttribute.keyword(
            "scm.operation.name", ObservationAttributePresence.EVENT_OPTIONAL, "Operation name.");

    private NabTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                PROVIDER_CODE, PROVIDER_NAME, PROVIDER_TYPE, PROVIDER_STATUS,
                PROVIDER_RESPONSE_CODE, PROVIDER_DURATION_MS, PROVIDER_ERROR_CODE, PROVIDER_ERROR_MESSAGE
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
