package ir.daneshrefah.scm.observation.metrics;

public final class ScmMetricNames {
    private ScmMetricNames() {
    }

    public static final String REQUESTS = "scm.requests";
    public static final String REQUEST_DURATION = "scm.request.duration";
    public static final String GATEWAY_REQUESTS = "scm.gateway.requests";
    public static final String SERVICE_EXECUTIONS = "scm.service.executions";
    public static final String SERVICE_DURATION = "scm.service.duration";
    public static final String OPERATION_CALLS = "scm.operation.calls";
    public static final String OPERATION_DURATION = "scm.operation.duration";
    public static final String PROVIDER_CALLS = "scm.provider.calls";
    public static final String PROVIDER_DURATION = "scm.provider.duration";
    public static final String PLUGIN_EXECUTIONS = "scm.plugin.executions";
    public static final String PLUGIN_DURATION = "scm.plugin.duration";
    public static final String TRANSFORM_EXECUTIONS = "scm.transform.executions";
    public static final String FAULTS = "scm.faults";
    public static final String CACHE_SERVER_STARTUP = "scm.cache.server.startup";
    public static final String CACHE_HAZELCAST_MEMBER_LIFECYCLE = "scm.cache.hazelcast.member.lifecycle";
    public static final String CACHE_MAP_CONFIG_LOAD = "scm.cache.map.config.load";
    public static final String CACHE_DISTRIBUTED_OBJECT_CREATE = "scm.cache.distributed.object.create";
    public static final String CACHE_ERRORS = "scm.cache.errors";
    public static final String CM_CONNECTOR_SESSION_READ = "scm.cm.connector.session.read";
    public static final String CM_CONNECTOR_CACHE_ACCESS = "scm.cm.connector.cache.access";
    public static final String CM_CONNECTOR_OWNERSHIP_CHECK = "scm.cm.connector.ownership.check";
    public static final String CM_CONNECTOR_OTP_VERIFY = "scm.cm.connector.otp.verify";
    public static final String CM_CONNECTOR_ERRORS = "scm.cm.connector.errors";
}
