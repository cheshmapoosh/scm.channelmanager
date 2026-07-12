package ir.daneshrefah.scm.provider.rest.trace;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.trace.TraceAttribute;

import java.util.List;

public final class RestTraceAttributes {
    private static final String OWNER = "scm-provider";

    public static final ObservationAttributeKey<String> PROVIDER_CODE = keyword("provider.code", "Provider code.");
    public static final ObservationAttributeKey<String> PROVIDER_NAME = keyword("provider.name", "Provider name.");
    public static final ObservationAttributeKey<String> PROVIDER_TYPE = keyword("provider.type", "Provider type.");
    public static final ObservationAttributeKey<String> PROVIDER_SCHEME = keyword("provider.scheme", "Provider scheme.");
    public static final ObservationAttributeKey<String> PROVIDER_OPERATION = keyword(
            "provider.operation", "Provider operation.");
    public static final ObservationAttributeKey<String> PROVIDER_ENDPOINT = keyword(
            "provider.endpoint", "Safe provider endpoint.");
    public static final ObservationAttributeKey<Long> PROVIDER_DURATION_MS = TraceAttribute.longNumber(
            "provider.duration_ms", OWNER, ObservationAttributePresence.EVENT_OPTIONAL,
            "Provider attempt duration in milliseconds.");
    public static final ObservationAttributeKey<String> PROVIDER_RESPONSE_CODE = keyword(
            "provider.response_code", "Provider response code.");
    public static final ObservationAttributeKey<String> PROVIDER_ERROR_CODE = keyword(
            "provider.error_code", "Provider error code.");

    private RestTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                PROVIDER_CODE, PROVIDER_NAME, PROVIDER_TYPE, PROVIDER_SCHEME,
                PROVIDER_OPERATION, PROVIDER_ENDPOINT,
                PROVIDER_DURATION_MS, PROVIDER_RESPONSE_CODE, PROVIDER_ERROR_CODE
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, String description) {
        return TraceAttribute.keyword(name, OWNER, ObservationAttributePresence.EVENT_OPTIONAL, description);
    }
}
