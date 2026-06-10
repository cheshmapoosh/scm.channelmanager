package ir.daneshrefah.scm.cache.observation;

import ir.daneshrefah.scm.observation.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.ObservationAttributeSensitivity;

import java.util.List;

public final class ScmCacheObservationAttributes {
    private static final String OWNER = "scm-cache";

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
        return List.of(HAZELCAST_MEMBER_ADDRESS, HAZELCAST_CLUSTER_SIZE);
    }
}
