package ir.daneshrefah.scm.observation.attributes.log;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

import java.util.List;

public final class CommonLogAttributes {
    public static final ObservationAttributeKey<String> TIMESTAMP = date("@timestamp", ObservationAttributePresence.ALWAYS_REQUIRED, "Log event timestamp.");
    public static final ObservationAttributeKey<String> LOG_LEVEL = keyword("log.level", ObservationAttributePresence.ALWAYS_REQUIRED, "Log severity level.");
    public static final ObservationAttributeKey<String> LOG_LOGGER = keyword("log.logger", ObservationAttributePresence.ALWAYS_REQUIRED, "Logger name.");
    public static final ObservationAttributeKey<String> PROCESS_THREAD_NAME = keyword("process.thread.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Thread name that emitted the log.");
    public static final ObservationAttributeKey<String> MESSAGE = text("message", ObservationAttributePresence.ALWAYS_REQUIRED, "Rendered log message.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_NAME = keyword("deployment.service.name", ObservationAttributePresence.CONTEXT_REQUIRED, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_VERSION = keyword("deployment.service.version", ObservationAttributePresence.CONTEXT_REQUIRED, "Running SCM service version.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = keyword("deployment.environment", ObservationAttributePresence.CONTEXT_REQUIRED, "Deployment environment.");
    public static final ObservationAttributeKey<String> SCM_RUNTIME = keyword("scm.runtime", ObservationAttributePresence.CONTEXT_REQUIRED, "Runtime mode.");
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

    private CommonLogAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                TIMESTAMP, LOG_LEVEL, LOG_LOGGER, PROCESS_THREAD_NAME, MESSAGE,
                DEPLOYMENT_SERVICE_NAME, DEPLOYMENT_SERVICE_VERSION, DEPLOYMENT_ENVIRONMENT, SCM_RUNTIME,
                CORRELATION_ID, CORRELATION_TYPE, EVENT_CATEGORY, EVENT_ACTION, EVENT_OUTCOME,
                TRACE_ID, SPAN_ID, ERROR_TYPE, ERROR_MESSAGE, ERROR_STACK_TRACE, ERROR_CODE, ERROR_CATEGORY
        );
    }

    private static ObservationAttributeKey<String> keyword(
            String name, ObservationAttributePresence presence, String description) {
        return LogAttribute.keyword(name, presence, description);
    }

    private static ObservationAttributeKey<String> text(
            String name, ObservationAttributePresence presence, String description) {
        return LogAttribute.text(name, presence, description);
    }

    private static ObservationAttributeKey<String> date(
            String name, ObservationAttributePresence presence, String description) {
        return LogAttribute.date(name, presence, description);
    }
}
