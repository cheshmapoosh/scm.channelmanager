package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationAttributeSensitivity;

import java.util.List;

public final class ScmCommonLogAttributes {
    private static final String OWNER = "common";

    public static final ObservationAttributeKey<String> TIMESTAMP = date("@timestamp", ObservationAttributePresence.ALWAYS_REQUIRED, "Log event timestamp.");
    public static final ObservationAttributeKey<String> LOG_LEVEL = keyword("log.level", ObservationAttributePresence.ALWAYS_REQUIRED, "Log severity level.");
    public static final ObservationAttributeKey<String> LOG_LOGGER = keyword("log.logger", ObservationAttributePresence.ALWAYS_REQUIRED, "Logger name.");
    public static final ObservationAttributeKey<String> PROCESS_THREAD_NAME = keyword("process.thread.name", ObservationAttributePresence.ALWAYS_REQUIRED, "Thread name that emitted the log.");
    public static final ObservationAttributeKey<String> MESSAGE = text("message", ObservationAttributePresence.ALWAYS_REQUIRED, "Rendered log message.");
    public static final ObservationAttributeKey<String> CORRELATION_ID = keyword("correlation.id", ObservationAttributePresence.ALWAYS_REQUIRED, "Correlation id for the active CorrelationType context.");
    public static final ObservationAttributeKey<String> CORRELATION_TYPE = keyword("correlation.type", ObservationAttributePresence.ALWAYS_REQUIRED, "Correlation type from the CorrelationType enum.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_NAME = keyword("deployment.service.name", ObservationAttributePresence.CONTEXT_REQUIRED, "Running SCM service name.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_SERVICE_VERSION = keyword("deployment.service.version", ObservationAttributePresence.CONTEXT_REQUIRED, "Running SCM service version.");
    public static final ObservationAttributeKey<String> DEPLOYMENT_ENVIRONMENT = keyword("deployment.environment", ObservationAttributePresence.CONTEXT_REQUIRED, "Deployment environment: dev, test, pilot or prod.");
    public static final ObservationAttributeKey<String> SCM_RUNTIME = keyword("scm.runtime", ObservationAttributePresence.CONTEXT_REQUIRED, "Runtime mode: standalone or kubernetes.");
    public static final ObservationAttributeKey<String> CONTAINER_IMAGE_NAME = keyword("container.image.name", ObservationAttributePresence.CONTEXT_OPTIONAL, "Injected container image name.");
    public static final ObservationAttributeKey<String> CONTAINER_IMAGE_TAG = keyword("container.image.tag", ObservationAttributePresence.CONTEXT_OPTIONAL, "Injected container image tag.");
    public static final ObservationAttributeKey<String> KUBERNETES_NAMESPACE = keyword("kubernetes.namespace", ObservationAttributePresence.CONTEXT_OPTIONAL, "Kubernetes namespace.");
    public static final ObservationAttributeKey<String> KUBERNETES_POD_NAME = keyword("kubernetes.pod.name", ObservationAttributePresence.CONTEXT_OPTIONAL, "Kubernetes pod name.");
    public static final ObservationAttributeKey<String> KUBERNETES_NODE_NAME = keyword("kubernetes.node.name", ObservationAttributePresence.CONTEXT_OPTIONAL, "Kubernetes node name.");
    public static final ObservationAttributeKey<String> EVENT_CATEGORY = keyword("event.category", ObservationAttributePresence.EVENT_REQUIRED, "SCM event category.");
    public static final ObservationAttributeKey<String> EVENT_ACTION = keyword("event.action", ObservationAttributePresence.EVENT_REQUIRED, "SCM event action.");
    public static final ObservationAttributeKey<String> EVENT_OUTCOME = keyword("event.outcome", ObservationAttributePresence.EVENT_REQUIRED, "SCM event outcome: success, failure, unknown or skipped.");
    public static final ObservationAttributeKey<String> TRACE_ID = keyword("trace.id", ObservationAttributePresence.EVENT_OPTIONAL, "Tracing id when a real tracing context exists.");
    public static final ObservationAttributeKey<String> SPAN_ID = keyword("span.id", ObservationAttributePresence.EVENT_OPTIONAL, "Span id when a real tracing context exists.");
    public static final ObservationAttributeKey<String> ERROR_TYPE = keyword("error.type", ObservationAttributePresence.ERROR_REQUIRED, "Throwable or fault type.");
    public static final ObservationAttributeKey<String> ERROR_MESSAGE = text("error.message", ObservationAttributePresence.ERROR_OPTIONAL, "Throwable or fault message.");
    public static final ObservationAttributeKey<String> ERROR_STACK_TRACE = text("error.stack_trace", ObservationAttributePresence.ERROR_OPTIONAL, "Throwable stack trace.");
    public static final ObservationAttributeKey<String> ERROR_CODE = keyword("error.code", ObservationAttributePresence.ERROR_OPTIONAL, "Application error code.");
    public static final ObservationAttributeKey<String> ERROR_CATEGORY = keyword("error.category", ObservationAttributePresence.ERROR_OPTIONAL, "Application error category.");

    private ScmCommonLogAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                TIMESTAMP,
                LOG_LEVEL,
                LOG_LOGGER,
                PROCESS_THREAD_NAME,
                MESSAGE,
                CORRELATION_ID,
                CORRELATION_TYPE,
                DEPLOYMENT_SERVICE_NAME,
                DEPLOYMENT_SERVICE_VERSION,
                DEPLOYMENT_ENVIRONMENT,
                SCM_RUNTIME,
                CONTAINER_IMAGE_NAME,
                CONTAINER_IMAGE_TAG,
                KUBERNETES_NAMESPACE,
                KUBERNETES_POD_NAME,
                KUBERNETES_NODE_NAME,
                EVENT_CATEGORY,
                EVENT_ACTION,
                EVENT_OUTCOME,
                TRACE_ID,
                SPAN_ID,
                ERROR_TYPE,
                ERROR_MESSAGE,
                ERROR_STACK_TRACE,
                ERROR_CODE,
                ERROR_CATEGORY
        );
    }

    private static ObservationAttributeKey<String> keyword(String name, ObservationAttributePresence presence, String description) {
        return ObservationAttributeKey.logString(name, ObservationAttributeKey.ELASTIC_KEYWORD, OWNER, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    private static ObservationAttributeKey<String> text(String name, ObservationAttributePresence presence, String description) {
        return ObservationAttributeKey.logString(name, ObservationAttributeKey.ELASTIC_TEXT, OWNER, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }

    private static ObservationAttributeKey<String> date(String name, ObservationAttributePresence presence, String description) {
        return ObservationAttributeKey.logString(name, ObservationAttributeKey.ELASTIC_DATE, OWNER, presence,
                ObservationAttributeSensitivity.RAW, 0, 0, description);
    }
}
