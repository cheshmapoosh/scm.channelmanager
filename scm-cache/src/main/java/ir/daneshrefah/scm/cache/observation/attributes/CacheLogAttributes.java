package ir.daneshrefah.scm.cache.observation.attributes;

import ir.daneshrefah.scm.observation.starter.ObservationAttributeKey;
import ir.daneshrefah.scm.observation.starter.ObservationAttributePresence;
import ir.daneshrefah.scm.observation.starter.attributes.log.LogAttribute;

import java.util.List;

public final class CacheLogAttributes {
    private static final String OWNER = "scm-cache";

    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_TYPE = keyword(
            "cache.hazelcast.element.type", ObservationAttributePresence.EVENT_OPTIONAL, "Hazelcast element type.");
    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_NAME = keyword(
            "cache.hazelcast.element.name", ObservationAttributePresence.EVENT_OPTIONAL, "Hazelcast element name.");
    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_CONFIG = text(
            "cache.hazelcast.element.config", ObservationAttributePresence.EVENT_OPTIONAL,
            "Selected safe Hazelcast element configuration as deterministic text.");
    public static final ObservationAttributeKey<Integer> HAZELCAST_ELEMENT_COUNT = integer(
            "cache.hazelcast.element.count", ObservationAttributePresence.EVENT_OPTIONAL,
            "Number of Hazelcast elements registered from cache configuration.");
    public static final ObservationAttributeKey<String> HAZELCAST_ELEMENT_SUMMARY = keyword(
            "cache.hazelcast.element.summary", ObservationAttributePresence.EVENT_OPTIONAL,
            "Low-cardinality summary of registered Hazelcast elements by type.");
    public static final ObservationAttributeKey<Integer> HAZELCAST_MATERIALIZED_COUNT = integer(
            "cache.hazelcast.materialized.count", ObservationAttributePresence.EVENT_OPTIONAL,
            "Number of Hazelcast distributed objects materialized from cache configuration.");
    public static final ObservationAttributeKey<String> HAZELCAST_MATERIALIZED_SUMMARY = keyword(
            "cache.hazelcast.materialized.summary", ObservationAttributePresence.EVENT_OPTIONAL,
            "Low-cardinality summary of materialized Hazelcast elements by type.");
    public static final ObservationAttributeKey<String> HAZELCAST_MEMBER_ADDRESS = keyword(
            "cache.hazelcast.member.address", ObservationAttributePresence.ON_CHANGE_OPTIONAL,
            "Hazelcast member address when the member address is created or changes.");
    public static final ObservationAttributeKey<Integer> HAZELCAST_CLUSTER_SIZE = integer(
            "cache.hazelcast.cluster.size", ObservationAttributePresence.ON_CHANGE_OPTIONAL,
            "Hazelcast cluster size when cluster membership is created or changes.");
    public static final ObservationAttributeKey<String> CONTAINER_IMAGE_NAME = keyword(
            "container.image.name", ObservationAttributePresence.CONTEXT_OPTIONAL, "Cache container image name.");
    public static final ObservationAttributeKey<String> CONTAINER_IMAGE_TAG = keyword(
            "container.image.tag", ObservationAttributePresence.CONTEXT_OPTIONAL, "Cache container image tag.");
    public static final ObservationAttributeKey<String> KUBERNETES_NAMESPACE = keyword(
            "kubernetes.namespace", ObservationAttributePresence.CONTEXT_OPTIONAL, "Cache Kubernetes namespace.");
    public static final ObservationAttributeKey<String> KUBERNETES_POD_NAME = keyword(
            "kubernetes.pod.name", ObservationAttributePresence.CONTEXT_OPTIONAL, "Cache Kubernetes pod name.");
    public static final ObservationAttributeKey<String> KUBERNETES_NODE_NAME = keyword(
            "kubernetes.node.name", ObservationAttributePresence.CONTEXT_OPTIONAL, "Cache Kubernetes node name.");

    private CacheLogAttributes() {
    }

    public static List<ObservationAttributeKey<?>> attributes() {
        return List.of(
                HAZELCAST_ELEMENT_TYPE, HAZELCAST_ELEMENT_NAME, HAZELCAST_ELEMENT_CONFIG,
                HAZELCAST_ELEMENT_COUNT, HAZELCAST_ELEMENT_SUMMARY,
                HAZELCAST_MATERIALIZED_COUNT, HAZELCAST_MATERIALIZED_SUMMARY,
                HAZELCAST_MEMBER_ADDRESS, HAZELCAST_CLUSTER_SIZE,
                CONTAINER_IMAGE_NAME, CONTAINER_IMAGE_TAG,
                KUBERNETES_NAMESPACE, KUBERNETES_POD_NAME, KUBERNETES_NODE_NAME
        );
    }

    private static ObservationAttributeKey<String> keyword(
            String name, ObservationAttributePresence presence, String description) {
        return LogAttribute.keyword(name, OWNER, presence, description);
    }

    private static ObservationAttributeKey<String> text(
            String name, ObservationAttributePresence presence, String description) {
        return LogAttribute.text(name, OWNER, presence, description);
    }

    private static ObservationAttributeKey<Integer> integer(
            String name, ObservationAttributePresence presence, String description) {
        return LogAttribute.integer(name, OWNER, presence, description);
    }
}
