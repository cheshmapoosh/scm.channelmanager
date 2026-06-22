package ir.daneshrefah.scm.cache.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationAttributeSensitivity;

import java.util.List;

public final class ScmCacheObservationAttributes {
    private static final String OWNER = "scm-cache";

    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_TYPE =
            ObservationAttributeKey.logString(
                    "cache.hazelcast.element.type",
                    ObservationAttributeKey.ELASTIC_KEYWORD,
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Hazelcast element type."
            );

    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_NAME =
            ObservationAttributeKey.logString(
                    "cache.hazelcast.element.name",
                    ObservationAttributeKey.ELASTIC_KEYWORD,
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Hazelcast element name."
            );

    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_CONFIG =
            ObservationAttributeKey.logString(
                    "cache.hazelcast.element.config",
                    ObservationAttributeKey.ELASTIC_TEXT,
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Selected safe Hazelcast element configuration as deterministic text."
            );

    public static final ObservationAttributeKey<Integer> HAZELCAST_ELEMENT_COUNT =
            ObservationAttributeKey.logInteger(
                    "cache.hazelcast.element.count",
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Number of Hazelcast elements registered from cache configuration."
            );

    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_SUMMARY =
            ObservationAttributeKey.logString(
                    "cache.hazelcast.element.summary",
                    ObservationAttributeKey.ELASTIC_KEYWORD,
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Low-cardinality summary of registered Hazelcast elements by type."
            );

    public static final ObservationAttributeKey<Integer> HAZELCAST_MATERIALIZED_COUNT =
            ObservationAttributeKey.logInteger(
                    "cache.hazelcast.materialized.count",
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Number of Hazelcast distributed objects materialized from cache configuration."
            );

    public static final ObservationAttributeKey<String> HAZELCAST_MATERIALIZED_SUMMARY =
            ObservationAttributeKey.logString(
                    "cache.hazelcast.materialized.summary",
                    ObservationAttributeKey.ELASTIC_KEYWORD,
                    OWNER,
                    ObservationAttributePresence.EVENT_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Low-cardinality summary of materialized Hazelcast elements by type."
            );

    public static final ObservationAttributeKey<String> HAZELCAST_MEMBER_ADDRESS =
            ObservationAttributeKey.logString(
                    "cache.hazelcast.member.address",
                    ObservationAttributeKey.ELASTIC_KEYWORD,
                    OWNER,
                    ObservationAttributePresence.ON_CHANGE_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Hazelcast member address when the member address is created or changes."
            );

    public static final ObservationAttributeKey<Integer> HAZELCAST_CLUSTER_SIZE =
            ObservationAttributeKey.logInteger(
                    "cache.hazelcast.cluster.size",
                    OWNER,
                    ObservationAttributePresence.ON_CHANGE_OPTIONAL,
                    ObservationAttributeSensitivity.RAW,
                    0,
                    0,
                    "Hazelcast cluster size when cluster membership is created or changes."
            );

    private ScmCacheObservationAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                HAZELCAST_ELEMENT_TYPE,
                HAZELCAST_ELEMENT_NAME,
                HAZELCAST_ELEMENT_CONFIG,
                HAZELCAST_ELEMENT_COUNT,
                HAZELCAST_ELEMENT_SUMMARY,
                HAZELCAST_MATERIALIZED_COUNT,
                HAZELCAST_MATERIALIZED_SUMMARY,
                HAZELCAST_MEMBER_ADDRESS,
                HAZELCAST_CLUSTER_SIZE
        );
    }
}
