package ir.daneshrefah.scm.observation.logging;

public final class ScmLogFields {
    public static final String TIMESTAMP = "@timestamp";
    public static final String LOG_LEVEL = "log.level";
    public static final String LOG_LOGGER = "log.logger";
    public static final String PROCESS_THREAD_NAME = "process.thread.name";
    public static final String MESSAGE = "message";
    public static final String EVENT_CATEGORY = "event.category";
    public static final String EVENT_ACTION = "event.action";
    public static final String EVENT_OUTCOME = "event.outcome";
    public static final String DEPLOYMENT_SERVICE_NAME = "deployment.service.name";
    public static final String DEPLOYMENT_SERVICE_VERSION = "deployment.service.version";
    public static final String DEPLOYMENT_ENVIRONMENT = "deployment.environment";
    public static final String SCM_RUNTIME = "scm.runtime";
    public static final String CONTAINER_IMAGE_NAME = "container.image.name";
    public static final String CONTAINER_IMAGE_TAG = "container.image.tag";
    public static final String KUBERNETES_NAMESPACE = "kubernetes.namespace";
    public static final String KUBERNETES_POD_NAME = "kubernetes.pod.name";
    public static final String KUBERNETES_NODE_NAME = "kubernetes.node.name";
    public static final String TRACE_ID = "trace.id";
    public static final String SPAN_ID = "span.id";
    public static final String CORRELATION_ID = "correlation.id";
    public static final String CORRELATION_TYPE = "correlation.type";
    public static final String ERROR_TYPE = "error.type";
    public static final String ERROR_MESSAGE = "error.message";
    public static final String ERROR_STACK_TRACE = "error.stack_trace";
    public static final String ERROR_CODE = "error.code";
    public static final String ERROR_CATEGORY = "error.category";

    private ScmLogFields() {
    }
}
