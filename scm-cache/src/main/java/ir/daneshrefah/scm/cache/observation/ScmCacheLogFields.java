package ir.daneshrefah.scm.cache.observation;

public final class ScmCacheLogFields {
    public static final String HAZELCAST_ELEMENT_TYPE =
            ScmCacheObservationAttributes.HAZELCAST_ELEMENT_TYPE.name();
    public static final String HAZELCAST_ELEMENT_NAME =
            ScmCacheObservationAttributes.HAZELCAST_ELEMENT_NAME.name();
    public static final String HAZELCAST_ELEMENT_COUNT =
            ScmCacheObservationAttributes.HAZELCAST_ELEMENT_COUNT.name();
    public static final String HAZELCAST_ELEMENT_SUMMARY =
            ScmCacheObservationAttributes.HAZELCAST_ELEMENT_SUMMARY.name();
    public static final String HAZELCAST_MATERIALIZED_COUNT =
            ScmCacheObservationAttributes.HAZELCAST_MATERIALIZED_COUNT.name();
    public static final String HAZELCAST_MATERIALIZED_SUMMARY =
            ScmCacheObservationAttributes.HAZELCAST_MATERIALIZED_SUMMARY.name();
    public static final String HAZELCAST_CLUSTER_SIZE =
            ScmCacheObservationAttributes.HAZELCAST_CLUSTER_SIZE.name();
    public static final String HAZELCAST_MEMBER_ADDRESS =
            ScmCacheObservationAttributes.HAZELCAST_MEMBER_ADDRESS.name();

    private ScmCacheLogFields() {
    }
}
