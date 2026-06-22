package ir.daneshrefah.scm.observation.attributes;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;

public final class ScmCacheAttributes {
    private ScmCacheAttributes() {
    }

    public static final ObservationAttributeKey<String> CLUSTER_NAME = ObservationAttributeKey.stringKey(
            "scm.cache.cluster.name",
            ObservationAttributePresence.EVENT_OPTIONAL,
            "Hazelcast cluster name"
    );
    public static final ObservationAttributeKey<String> INSTANCE_NAME = ObservationAttributeKey.stringKey(
            "scm.cache.instance.name",
            ObservationAttributePresence.EVENT_OPTIONAL,
            "Hazelcast member instance name"
    );
    public static final ObservationAttributeKey<Integer> DEFINITION_COUNT = ObservationAttributeKey.integerKey(
            "scm.cache.definition.count",
            ObservationAttributePresence.EVENT_OPTIONAL,
            "Number of cache definitions loaded from storage"
    );
    public static final ObservationAttributeKey<Integer> ELEMENT_COUNT = ObservationAttributeKey.integerKey(
            "scm.cache.element.count",
            ObservationAttributePresence.EVENT_OPTIONAL,
            "Number of Hazelcast elements registered during initialization"
    );
    public static final ObservationAttributeKey<Integer> DISTRIBUTED_OBJECT_COUNT = ObservationAttributeKey.integerKey(
            "cache.hazelcast.cluster.size",
            ObservationAttributePresence.ON_CHANGE_OPTIONAL,
            "Hazelcast cluster size"
    );
    public static final ObservationAttributeKey<Object> ELEMENT_SUMMARY = ObservationAttributeKey.objectKey(
            "scm.cache.element.summary",
            ObservationAttributePresence.EVENT_OPTIONAL,
            "Hazelcast element counts grouped by type"
    );
    public static final ObservationAttributeKey<String> INITIALIZATION_DURATION_MS = ObservationAttributeKey.stringKey(
            "cache.hazelcast.member.address",
            ObservationAttributePresence.ON_CHANGE_OPTIONAL,
            "Hazelcast member address"
    );
}
