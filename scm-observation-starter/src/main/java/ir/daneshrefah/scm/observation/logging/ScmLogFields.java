package ir.daneshrefah.scm.observation.logging;

import ir.daneshrefah.scm.observation.attributes.log.CommonLogAttributes;

public final class ScmLogFields {
    public static final String TIMESTAMP = CommonLogAttributes.TIMESTAMP.name();
    public static final String LOG_LEVEL = CommonLogAttributes.LOG_LEVEL.name();
    public static final String LOG_LOGGER = CommonLogAttributes.LOG_LOGGER.name();
    public static final String PROCESS_THREAD_NAME = CommonLogAttributes.PROCESS_THREAD_NAME.name();
    public static final String MESSAGE = CommonLogAttributes.MESSAGE.name();
    public static final String EVENT_STREAM = CommonLogAttributes.EVENT_STREAM.name();
    public static final String SCM_OBS_TARGET_NAMESPACE = CommonLogAttributes.SCM_OBS_TARGET_NAMESPACE.name();
    public static final String SCM_OBS_TARGET_INDEX = CommonLogAttributes.SCM_OBS_TARGET_INDEX.name();
    public static final String SCM_PLATFORM = CommonLogAttributes.SCM_PLATFORM.name();
    public static final String SERVICE_NAME = CommonLogAttributes.SERVICE_NAME.name();
    public static final String EVENT_CATEGORY = CommonLogAttributes.EVENT_CATEGORY.name();
    public static final String EVENT_ACTION = CommonLogAttributes.EVENT_ACTION.name();
    public static final String EVENT_OUTCOME = CommonLogAttributes.EVENT_OUTCOME.name();
    public static final String DEPLOYMENT_SERVICE_NAME = CommonLogAttributes.DEPLOYMENT_SERVICE_NAME.name();
    public static final String DEPLOYMENT_SERVICE_VERSION = CommonLogAttributes.DEPLOYMENT_SERVICE_VERSION.name();
    public static final String DEPLOYMENT_ENVIRONMENT = CommonLogAttributes.DEPLOYMENT_ENVIRONMENT.name();
    public static final String SCM_RUNTIME = CommonLogAttributes.SCM_RUNTIME.name();
    public static final String SCM_CHANNEL_CODE = CommonLogAttributes.SCM_CHANNEL_CODE.name();
    public static final String SCM_OBS_LEGACY_ENABLED = CommonLogAttributes.SCM_OBS_LEGACY_ENABLED.name();
    public static final String SCM_OBS_LEGACY_SERVICE_CODE = CommonLogAttributes.SCM_OBS_LEGACY_SERVICE_CODE.name();
    public static final String SCM_OBS_LEGACY_OPERATION_CODE = CommonLogAttributes.SCM_OBS_LEGACY_OPERATION_CODE.name();
    public static final String TRACE_ID = CommonLogAttributes.TRACE_ID.name();
    public static final String SPAN_ID = CommonLogAttributes.SPAN_ID.name();
    public static final String CORRELATION_ID = CommonLogAttributes.CORRELATION_ID.name();
    public static final String CORRELATION_TYPE = CommonLogAttributes.CORRELATION_TYPE.name();
    public static final String ERROR_TYPE = CommonLogAttributes.ERROR_TYPE.name();
    public static final String ERROR_MESSAGE = CommonLogAttributes.ERROR_MESSAGE.name();
    public static final String ERROR_STACK_TRACE = CommonLogAttributes.ERROR_STACK_TRACE.name();
    public static final String ERROR_CODE = CommonLogAttributes.ERROR_CODE.name();
    public static final String ERROR_CATEGORY = CommonLogAttributes.ERROR_CATEGORY.name();

    private ScmLogFields() {
    }
}
