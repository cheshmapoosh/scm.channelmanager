package ir.daneshrefah.scm.observation.starter.attributes.audit;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;

import java.util.List;

public final class ChangeEntityAuditAttributes {
    public static final String TYPE_VALUE = "CHANGE_ENTITY";

    public static final ObservationAttributeKey<String> TIMESTAMP = date("@timestamp", ObservationAttributePresence.ALWAYS_REQUIRED, "Audit event timestamp.");
    public static final ObservationAttributeKey<String> MESSAGE = text("message", ObservationAttributePresence.ALWAYS_REQUIRED, "Audit message.");
    public static final ObservationAttributeKey<String> EVENT_STREAM = keyword("event.stream", ObservationAttributePresence.ALWAYS_REQUIRED, "Observation stream name.");
    public static final ObservationAttributeKey<String> SCM_OBS_TARGET_NAMESPACE = keyword("scm.observation.target.namespace", ObservationAttributePresence.ALWAYS_REQUIRED, "Observation target namespace.");
    public static final ObservationAttributeKey<String> SCM_OBS_TARGET_INDEX = keyword("scm.observation.target.index", ObservationAttributePresence.ALWAYS_REQUIRED, "Final Elasticsearch routing index.");
    public static final ObservationAttributeKey<String> SCM_PLATFORM = keyword("scm.platform", ObservationAttributePresence.ALWAYS_REQUIRED, "SCM platform code.");
    public static final ObservationAttributeKey<String> SERVICE_NAME = keyword("service.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_NAME = keyword("deployment.service.name", ObservationAttributePresence.EVENT_OPTIONAL, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_VERSION = keyword("deployment.service.version", ObservationAttributePresence.EVENT_OPTIONAL, "Running SCM service version.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = keyword("deployment.environment", ObservationAttributePresence.ALWAYS_REQUIRED, "Deployment environment.");
    public static final ObservationAttributeKey<String> SCM_RUNTIME = keyword("scm.runtime", ObservationAttributePresence.EVENT_OPTIONAL, "Runtime mode.");
    public static final ObservationAttributeKey<Boolean> SCM_OBS_LEGACY_ENABLED = AuditAttribute.booleanValue(
            "scm.observation.legacy.enabled", ObservationAttributePresence.EVENT_OPTIONAL, "Whether this record is eligible for legacy projection.");
    public static final ObservationAttributeKey<String> SCM_OBS_LEGACY_SERVICE_CODE = keyword(
            "scm.observation.legacy.service.code", ObservationAttributePresence.EVENT_OPTIONAL, "Explicit legacy service code.");
    public static final ObservationAttributeKey<String> SCM_OBS_LEGACY_OPERATION_CODE = keyword(
            "scm.observation.legacy.operation.code", ObservationAttributePresence.EVENT_OPTIONAL, "Explicit legacy operation code.");
    public static final ObservationAttributeKey<String> CORRELATION_ID = keyword("correlation.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Correlation identifier.");
    public static final ObservationAttributeKey<String> CORRELATION_TYPE = keyword("correlation.type", ObservationAttributePresence.ALWAYS_REQUIRED, "Correlation type.");
    public static final ObservationAttributeKey<String> EVENT_CATEGORY = keyword("event.category", ObservationAttributePresence.EVENT_REQUIRED, "Event category.");
    public static final ObservationAttributeKey<String> EVENT_ACTION = keyword("event.action", ObservationAttributePresence.EVENT_REQUIRED, "Event action.");
    public static final ObservationAttributeKey<String> EVENT_OUTCOME = keyword("event.outcome", ObservationAttributePresence.EVENT_REQUIRED, "Event outcome.");
    public static final ObservationAttributeKey<String> TRACE_ID = keyword("trace.id", ObservationAttributePresence.EVENT_OPTIONAL, "Trace identifier.");
    public static final ObservationAttributeKey<String> SPAN_ID = keyword("span.id", ObservationAttributePresence.EVENT_OPTIONAL, "Span identifier.");
    public static final ObservationAttributeKey<String> ERROR_TYPE = keyword("error.type", ObservationAttributePresence.ERROR_REQUIRED, "Error type.");
    public static final ObservationAttributeKey<String> ERROR_MESSAGE = text("error.message", ObservationAttributePresence.ERROR_OPTIONAL, "Safe error message.");
    public static final ObservationAttributeKey<String> ERROR_STACK_TRACE = text("error.stack_trace", ObservationAttributePresence.ERROR_OPTIONAL, "Error stack trace.");
    public static final ObservationAttributeKey<String> ERROR_CODE = keyword("error.code", ObservationAttributePresence.ERROR_OPTIONAL, "Application error code.");
    public static final ObservationAttributeKey<String> ERROR_CATEGORY = keyword("error.category", ObservationAttributePresence.ERROR_OPTIONAL, "Application error category.");

    public static final ObservationAttributeKey<String> AUDIT_TYPE = keyword("audit.type", ObservationAttributePresence.ALWAYS_REQUIRED, "Audit model type.");
    public static final ObservationAttributeKey<String> ACTOR_TYPE = keyword("actor.type", ObservationAttributePresence.EVENT_OPTIONAL, "Actor type.");
    public static final ObservationAttributeKey<String> ACTOR_ID = keyword("actor.id", ObservationAttributePresence.EVENT_OPTIONAL, "Actor identifier.");
    public static final ObservationAttributeKey<String> ACTOR_USERNAME_MASKED = masked("actor.username.masked", "Masked actor username.");
    public static final ObservationAttributeKey<String> ENTITY_TYPE = keyword("entity.type", ObservationAttributePresence.EVENT_OPTIONAL, "Changed entity type.");
    public static final ObservationAttributeKey<String> ENTITY_ID = keyword("entity.id", ObservationAttributePresence.EVENT_OPTIONAL, "Changed entity identifier.");
    public static final ObservationAttributeKey<String> ENTITY_CODE = keyword("entity.code", ObservationAttributePresence.EVENT_OPTIONAL, "Changed entity code.");
    public static final ObservationAttributeKey<String> CHANGE_ACTION = keyword("change.action", ObservationAttributePresence.EVENT_OPTIONAL, "Change action.");
    public static final ObservationAttributeKey<String> CHANGE_FIELD = keyword("change.field", ObservationAttributePresence.EVENT_OPTIONAL, "Changed field.");
    public static final ObservationAttributeKey<String> CHANGE_OLD_VALUE_MASKED = masked("change.old.value.masked", "Masked previous value.");
    public static final ObservationAttributeKey<String> CHANGE_NEW_VALUE_MASKED = masked("change.new.value.masked", "Masked new value.");

    private ChangeEntityAuditAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                TIMESTAMP, MESSAGE,
                EVENT_STREAM, SCM_OBS_TARGET_NAMESPACE, SCM_OBS_TARGET_INDEX, SCM_PLATFORM, SERVICE_NAME,
                DEPLOYMENT_SERVICE_NAME, DEPLOYMENT_SERVICE_VERSION, DEPLOYMENT_ENVIRONMENT, SCM_RUNTIME,
                SCM_OBS_LEGACY_ENABLED, SCM_OBS_LEGACY_SERVICE_CODE, SCM_OBS_LEGACY_OPERATION_CODE,
                CORRELATION_ID, CORRELATION_TYPE, EVENT_CATEGORY, EVENT_ACTION, EVENT_OUTCOME,
                TRACE_ID, SPAN_ID, ERROR_TYPE, ERROR_MESSAGE, ERROR_STACK_TRACE, ERROR_CODE, ERROR_CATEGORY,
                AUDIT_TYPE, ACTOR_TYPE, ACTOR_ID, ACTOR_USERNAME_MASKED,
                ENTITY_TYPE, ENTITY_ID, ENTITY_CODE, CHANGE_ACTION, CHANGE_FIELD,
                CHANGE_OLD_VALUE_MASKED, CHANGE_NEW_VALUE_MASKED
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, ObservationAttributePresence presence, String description) {
        return AuditAttribute.keyword(name, presence, description);
    }

    private static ObservationAttributeKey<String> text(String name, ObservationAttributePresence presence, String description) {
        return AuditAttribute.text(name, presence, description);
    }

    private static ObservationAttributeKey<String> date(String name, ObservationAttributePresence presence, String description) {
        return AuditAttribute.date(name, presence, description);
    }

    private static ObservationAttributeKey<String> masked(String name, String description) {
        return AuditAttribute.maskedKeyword(name, ObservationAttributePresence.EVENT_OPTIONAL, 0, 0, description);
    }
}
