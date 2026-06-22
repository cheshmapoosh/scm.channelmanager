package ir.daneshrefah.scm.observation.logging;

import ir.daneshrefah.scm.observation.attributes.ScmCommonLogAttributes;

public final class ScmLogFields {
    public static final String TIMESTAMP = ScmCommonLogAttributes.TIMESTAMP.name();
    public static final String LOG_LEVEL = ScmCommonLogAttributes.LOG_LEVEL.name();
    public static final String LOG_LOGGER = ScmCommonLogAttributes.LOG_LOGGER.name();
    public static final String PROCESS_THREAD_NAME = ScmCommonLogAttributes.PROCESS_THREAD_NAME.name();
    public static final String MESSAGE = ScmCommonLogAttributes.MESSAGE.name();
    public static final String EVENT_CATEGORY = ScmCommonLogAttributes.EVENT_CATEGORY.name();
    public static final String EVENT_ACTION = ScmCommonLogAttributes.EVENT_ACTION.name();
    public static final String EVENT_OUTCOME = ScmCommonLogAttributes.EVENT_OUTCOME.name();
    public static final String DEPLOYMENT_SERVICE_NAME = ScmCommonLogAttributes.DEPLOYMENT_SERVICE_NAME.name();
    public static final String DEPLOYMENT_SERVICE_VERSION = ScmCommonLogAttributes.DEPLOYMENT_SERVICE_VERSION.name();
    public static final String DEPLOYMENT_ENVIRONMENT = ScmCommonLogAttributes.DEPLOYMENT_ENVIRONMENT.name();
    public static final String SCM_RUNTIME = ScmCommonLogAttributes.SCM_RUNTIME.name();
    public static final String CONTAINER_IMAGE_NAME = ScmCommonLogAttributes.CONTAINER_IMAGE_NAME.name();
    public static final String CONTAINER_IMAGE_TAG = ScmCommonLogAttributes.CONTAINER_IMAGE_TAG.name();
    public static final String KUBERNETES_NAMESPACE = ScmCommonLogAttributes.KUBERNETES_NAMESPACE.name();
    public static final String KUBERNETES_POD_NAME = ScmCommonLogAttributes.KUBERNETES_POD_NAME.name();
    public static final String KUBERNETES_NODE_NAME = ScmCommonLogAttributes.KUBERNETES_NODE_NAME.name();
    public static final String TRACE_ID = ScmCommonLogAttributes.TRACE_ID.name();
    public static final String SPAN_ID = ScmCommonLogAttributes.SPAN_ID.name();
    public static final String CORRELATION_ID = ScmCommonLogAttributes.CORRELATION_ID.name();
    public static final String CORRELATION_TYPE = ScmCommonLogAttributes.CORRELATION_TYPE.name();
    public static final String ERROR_TYPE = ScmCommonLogAttributes.ERROR_TYPE.name();
    public static final String ERROR_MESSAGE = ScmCommonLogAttributes.ERROR_MESSAGE.name();
    public static final String ERROR_STACK_TRACE = ScmCommonLogAttributes.ERROR_STACK_TRACE.name();
    public static final String ERROR_CODE = ScmCommonLogAttributes.ERROR_CODE.name();
    public static final String ERROR_CATEGORY = ScmCommonLogAttributes.ERROR_CATEGORY.name();

    private ScmLogFields() {
    }
}
