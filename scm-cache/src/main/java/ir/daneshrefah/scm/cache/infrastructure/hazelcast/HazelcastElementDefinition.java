package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

public record HazelcastElementDefinition(
        HazelcastElementType type,
        String name
) {
    public HazelcastElementDefinition {
        if (type == null) {
            throw new IllegalArgumentException("Hazelcast element type must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hazelcast element name must not be blank");
        }
        name = name.trim();
    }
}
