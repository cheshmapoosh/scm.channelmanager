package ir.daneshrefah.scm.cache.observation;

public final class ScmCacheObservationEvents {
    public static final String SCM_CACHE_CONTEXT = "scm.cache.context";
    public static final String SCM_CACHE_INIT = "scm.cache.init";
    public static final String SCM_CACHE_HEALTH = "scm.cache.health";

    public static final String RUNTIME_CONTEXT_CREATED = "runtime.context.created";
    public static final String SCM_CACHE_INIT_STARTED = "scm.cache.init.started";
    public static final String HAZELCAST_BOOTSTRAP_STARTED = "hazelcast.bootstrap.started";
    public static final String HAZELCAST_BOOTSTRAP_CONFIG_LOADED = "hazelcast.bootstrap.config.loaded";
    public static final String HAZELCAST_BOOTSTRAP_CONFIG_REGISTERED = "hazelcast.bootstrap.config.registered";
    public static final String HAZELCAST_BOOTSTRAP_MEMBER_STARTED = "hazelcast.bootstrap.member.started";
    public static final String HAZELCAST_BOOTSTRAP_OBJECTS_MATERIALIZED = "hazelcast.bootstrap.objects.materialized";
    public static final String HAZELCAST_BOOTSTRAP_COMPLETED = "hazelcast.bootstrap.completed";
    public static final String HAZELCAST_BOOTSTRAP_FAILED = "hazelcast.bootstrap.failed";
    public static final String HAZELCAST_ELEMENT_REGISTERED = "hazelcast.element.registered";
    public static final String HAZELCAST_ELEMENT_MATERIALIZED = "hazelcast.element.materialized";
    public static final String HAZELCAST_HEALTH_CHANGED = "hazelcast.health.changed";

    private ScmCacheObservationEvents() {
    }
}
