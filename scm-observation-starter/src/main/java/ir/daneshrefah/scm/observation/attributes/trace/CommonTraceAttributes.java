package ir.daneshrefah.scm.observation.attributes.trace;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

import java.util.List;

public final class CommonTraceAttributes {
    public static final ObservationAttributeKey<String> TIMESTAMP = date("@timestamp", ObservationAttributePresence.ALWAYS_REQUIRED, "Trace event timestamp.");
    public static final ObservationAttributeKey<String> MESSAGE = text("message", ObservationAttributePresence.ALWAYS_REQUIRED, "Trace message.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_NAME = keyword("deployment.service.name", ObservationAttributePresence.EVENT_OPTIONAL, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_VERSION = keyword("deployment.service.version", ObservationAttributePresence.EVENT_OPTIONAL, "Running SCM service version.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = keyword("deployment.environment", ObservationAttributePresence.EVENT_OPTIONAL, "Deployment environment.");
    public static final ObservationAttributeKey<String> SCM_RUNTIME = keyword("scm.runtime", ObservationAttributePresence.EVENT_OPTIONAL, "Runtime mode.");
    public static final ObservationAttributeKey<String> CORRELATION_ID = keyword("correlation.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Correlation identifier.");
    public static final ObservationAttributeKey<String> CORRELATION_TYPE = keyword("correlation.type", ObservationAttributePresence.ALWAYS_REQUIRED, "Correlation type.");
    public static final ObservationAttributeKey<String> TRACE_ID = keyword("trace.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Trace identifier.");
    public static final ObservationAttributeKey<String> SPAN_ID = keyword("span.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Span identifier.");
    public static final ObservationAttributeKey<String> PARENT_SPAN_ID = keyword("parent.span.id", ObservationAttributePresence.EVENT_OPTIONAL, "Parent span identifier; absent means root span.");
    public static final ObservationAttributeKey<String> SPAN_NAME = keyword("span.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Span name.");
    public static final ObservationAttributeKey<String> SPAN_KIND = keyword("span.kind", ObservationAttributePresence.ALWAYS_REQUIRED, "Span kind.");
    public static final ObservationAttributeKey<String> SPAN_START_TIME = date("span.start_time", ObservationAttributePresence.ALWAYS_REQUIRED, "Span start time.");
    public static final ObservationAttributeKey<String> SPAN_END_TIME = date("span.end_time", ObservationAttributePresence.ALWAYS_REQUIRED, "Span end time.");
    public static final ObservationAttributeKey<Long> SPAN_DURATION_MS = TraceAttribute.longNumber("span.duration_ms", ObservationAttributePresence.ALWAYS_REQUIRED, "Span duration in milliseconds.");
    public static final ObservationAttributeKey<String> EVENT_CATEGORY = keyword("event.category", ObservationAttributePresence.EVENT_REQUIRED, "Event category.");
    public static final ObservationAttributeKey<String> EVENT_ACTION = keyword("event.action", ObservationAttributePresence.EVENT_REQUIRED, "Event action.");
    public static final ObservationAttributeKey<String> EVENT_OUTCOME = keyword("event.outcome", ObservationAttributePresence.EVENT_REQUIRED, "Event outcome.");
    public static final ObservationAttributeKey<String> ERROR_TYPE = keyword("error.type", ObservationAttributePresence.ERROR_REQUIRED, "Error type.");
    public static final ObservationAttributeKey<String> ERROR_MESSAGE = text("error.message", ObservationAttributePresence.ERROR_OPTIONAL, "Safe error message.");
    public static final ObservationAttributeKey<String> ERROR_STACK_TRACE = text("error.stack_trace", ObservationAttributePresence.ERROR_OPTIONAL, "Error stack trace.");
    public static final ObservationAttributeKey<String> ERROR_CODE = keyword("error.code", ObservationAttributePresence.ERROR_OPTIONAL, "Application error code.");
    public static final ObservationAttributeKey<String> ERROR_CATEGORY = keyword("error.category", ObservationAttributePresence.ERROR_OPTIONAL, "Application error category.");

    private CommonTraceAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                TIMESTAMP, MESSAGE,
                DEPLOYMENT_SERVICE_NAME, DEPLOYMENT_SERVICE_VERSION, DEPLOYMENT_ENVIRONMENT, SCM_RUNTIME,
                CORRELATION_ID, CORRELATION_TYPE, TRACE_ID, SPAN_ID, PARENT_SPAN_ID,
                SPAN_NAME, SPAN_KIND, SPAN_START_TIME, SPAN_END_TIME, SPAN_DURATION_MS,
                EVENT_CATEGORY, EVENT_ACTION, EVENT_OUTCOME,
                ERROR_TYPE, ERROR_MESSAGE, ERROR_STACK_TRACE, ERROR_CODE, ERROR_CATEGORY
        );
    }

    private static ObservationAttributeKey<String> keyword(
            String name, ObservationAttributePresence presence, String description) {
        return TraceAttribute.keyword(name, presence, description);
    }

    private static ObservationAttributeKey<String> text(
            String name, ObservationAttributePresence presence, String description) {
        return TraceAttribute.text(name, presence, description);
    }

    private static ObservationAttributeKey<String> date(
            String name, ObservationAttributePresence presence, String description) {
        return TraceAttribute.date(name, presence, description);
    }
}
