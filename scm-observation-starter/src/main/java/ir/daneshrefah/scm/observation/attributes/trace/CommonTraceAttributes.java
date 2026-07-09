package ir.daneshrefah.scm.observation.attributes.trace;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

import java.util.List;

public final class CommonTraceAttributes {
    public static final ObservationAttributeKey<String> TIMESTAMP = date("@timestamp", ObservationAttributePresence.ALWAYS_REQUIRED, "Trace event timestamp.");
    public static final ObservationAttributeKey<String> MESSAGE = text("message", ObservationAttributePresence.ALWAYS_REQUIRED, "Trace message.");
    public static final ObservationAttributeKey<String> EVENT_STREAM = keyword("event.stream", ObservationAttributePresence.ALWAYS_REQUIRED, "Observation stream name.");
    public static final ObservationAttributeKey<String> SCM_OBS_TARGET_NAMESPACE = keyword("scm.observation.target.namespace", ObservationAttributePresence.ALWAYS_REQUIRED, "Observation target namespace.");
    public static final ObservationAttributeKey<String> SCM_OBS_TARGET_INDEX = keyword("scm.observation.target.index", ObservationAttributePresence.ALWAYS_REQUIRED, "Final Elasticsearch routing index.");
    public static final ObservationAttributeKey<String> SCM_PLATFORM = keyword("scm.platform", ObservationAttributePresence.ALWAYS_REQUIRED, "SCM platform code.");
    public static final ObservationAttributeKey<String> SERVICE_NAME = keyword("service.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_NAME = keyword("deployment.service.name", ObservationAttributePresence.EVENT_OPTIONAL, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_VERSION = keyword("deployment.service.version", ObservationAttributePresence.EVENT_OPTIONAL, "Running SCM service version.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = keyword("deployment.environment", ObservationAttributePresence.ALWAYS_REQUIRED, "Deployment environment.");
    public static final ObservationAttributeKey<String> SCM_RUNTIME = keyword("scm.runtime", ObservationAttributePresence.EVENT_OPTIONAL, "Runtime mode.");
    public static final ObservationAttributeKey<String> SCM_CHANNEL_CODE = keyword("scm.channel.code", ObservationAttributePresence.EVENT_OPTIONAL, "Business channel code.");
    public static final ObservationAttributeKey<Boolean> SCM_OBS_LEGACY_ENABLED = TraceAttribute.booleanValue(
            "scm.observation.legacy.enabled", ObservationAttributePresence.EVENT_OPTIONAL, "Whether this record is eligible for legacy projection.");
    public static final ObservationAttributeKey<String> SCM_OBS_LEGACY_SERVICE_CODE = keyword(
            "scm.observation.legacy.service.code", ObservationAttributePresence.EVENT_OPTIONAL, "Explicit legacy service code.");
    public static final ObservationAttributeKey<String> SCM_OBS_LEGACY_OPERATION_CODE = keyword(
            "scm.observation.legacy.operation.code", ObservationAttributePresence.EVENT_OPTIONAL, "Explicit legacy operation code.");
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
    public static final ObservationAttributeKey<String> HTTP_METHOD = keyword("http.method", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP method.");
    public static final ObservationAttributeKey<String> HTTP_ROUTE = keyword("http.route", ObservationAttributePresence.EVENT_OPTIONAL, "Low-cardinality HTTP route template.");
    public static final ObservationAttributeKey<String> URL_PATH = keyword("url.path", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP request path without query string.");
    public static final ObservationAttributeKey<Integer> HTTP_STATUS_CODE = TraceAttribute.integerNumber("http.status_code", ObservationAttributePresence.EVENT_OPTIONAL, "HTTP response status code.");
    public static final ObservationAttributeKey<Boolean> HTTP_QUERY_PRESENT = TraceAttribute.booleanValue(
            "http.query.present", ObservationAttributePresence.EVENT_OPTIONAL, "Whether an HTTP query string was present.");
    public static final ObservationAttributeKey<String> CLIENT_IP = keyword("client.ip", ObservationAttributePresence.EVENT_OPTIONAL, "Client IP address.");
    public static final ObservationAttributeKey<String> CLIENT_ADDRESS = keyword("client.address", ObservationAttributePresence.EVENT_OPTIONAL, "Protocol-neutral client address.");
    public static final ObservationAttributeKey<String> SCM_GATEWAY_NAME = keyword("scm.gateway.name", ObservationAttributePresence.EVENT_OPTIONAL, "SCM gateway name.");
    public static final ObservationAttributeKey<String> SCM_PROTOCOL = keyword("scm.protocol", ObservationAttributePresence.EVENT_OPTIONAL, "Gateway protocol.");
    public static final ObservationAttributeKey<String> SCM_REQUEST_NAME = keyword("scm.request.name", ObservationAttributePresence.EVENT_OPTIONAL, "Low-cardinality gateway request name.");
    public static final ObservationAttributeKey<String> SCM_MESSAGE_ID = keyword("scm.message.id", ObservationAttributePresence.EVENT_OPTIONAL, "Protocol message identifier.");
    public static final ObservationAttributeKey<String> SCM_ROUTE_ID = keyword("scm.route.id", ObservationAttributePresence.EVENT_OPTIONAL, "Gateway route identifier.");
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
                EVENT_STREAM, SCM_OBS_TARGET_NAMESPACE, SCM_OBS_TARGET_INDEX, SCM_PLATFORM, SERVICE_NAME,
                DEPLOYMENT_SERVICE_NAME, DEPLOYMENT_SERVICE_VERSION, DEPLOYMENT_ENVIRONMENT, SCM_RUNTIME,
                SCM_CHANNEL_CODE, SCM_OBS_LEGACY_ENABLED, SCM_OBS_LEGACY_SERVICE_CODE, SCM_OBS_LEGACY_OPERATION_CODE,
                CORRELATION_ID, CORRELATION_TYPE, TRACE_ID, SPAN_ID, PARENT_SPAN_ID,
                SPAN_NAME, SPAN_KIND, SPAN_START_TIME, SPAN_END_TIME, SPAN_DURATION_MS,
                HTTP_METHOD, HTTP_ROUTE, URL_PATH, HTTP_STATUS_CODE, HTTP_QUERY_PRESENT, CLIENT_IP, CLIENT_ADDRESS,
                SCM_GATEWAY_NAME, SCM_PROTOCOL, SCM_REQUEST_NAME, SCM_MESSAGE_ID, SCM_ROUTE_ID,
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
