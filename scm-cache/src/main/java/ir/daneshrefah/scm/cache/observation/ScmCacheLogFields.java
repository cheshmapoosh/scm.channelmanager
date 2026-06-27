package ir.daneshrefah.scm.cache.observation;

import ir.daneshrefah.scm.cache.observation.attributes.CacheLogAttributes;

public final class ScmCacheLogFields {
    public static final String HAZELCAST_ELEMENT_TYPE =
            CacheLogAttributes.HAZELCAST_ELEMENT_TYPE.name();
    public static final String HAZELCAST_ELEMENT_NAME =
            CacheLogAttributes.HAZELCAST_ELEMENT_NAME.name();
    public static final String HAZELCAST_ELEMENT_CONFIG =
            CacheLogAttributes.HAZELCAST_ELEMENT_CONFIG.name();
    public static final String HAZELCAST_ELEMENT_COUNT =
            CacheLogAttributes.HAZELCAST_ELEMENT_COUNT.name();
    public static final String HAZELCAST_ELEMENT_SUMMARY =
            CacheLogAttributes.HAZELCAST_ELEMENT_SUMMARY.name();
    public static final String HAZELCAST_MATERIALIZED_COUNT =
            CacheLogAttributes.HAZELCAST_MATERIALIZED_COUNT.name();
    public static final String HAZELCAST_MATERIALIZED_SUMMARY =
            CacheLogAttributes.HAZELCAST_MATERIALIZED_SUMMARY.name();
    public static final String HAZELCAST_CLUSTER_SIZE =
            CacheLogAttributes.HAZELCAST_CLUSTER_SIZE.name();
    public static final String HAZELCAST_MEMBER_ADDRESS =
            CacheLogAttributes.HAZELCAST_MEMBER_ADDRESS.name();
    public static final String CONTAINER_IMAGE_NAME = CacheLogAttributes.CONTAINER_IMAGE_NAME.name();
    public static final String CONTAINER_IMAGE_TAG = CacheLogAttributes.CONTAINER_IMAGE_TAG.name();
    public static final String KUBERNETES_NAMESPACE = CacheLogAttributes.KUBERNETES_NAMESPACE.name();
    public static final String KUBERNETES_POD_NAME = CacheLogAttributes.KUBERNETES_POD_NAME.name();
    public static final String KUBERNETES_NODE_NAME = CacheLogAttributes.KUBERNETES_NODE_NAME.name();

    private ScmCacheLogFields() {
    }
}
