package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

public class HazelcastElementMaterializationException extends RuntimeException {
    private final HazelcastElementType elementType;
    private final String elementName;

    public HazelcastElementMaterializationException(
            HazelcastElementType elementType,
            String elementName,
            Throwable cause
    ) {
        super("Failed to materialize Hazelcast " + elementType + " element: " + elementName, cause);
        this.elementType = elementType;
        this.elementName = elementName;
    }

    public HazelcastElementType elementType() {
        return elementType;
    }

    public String elementName() {
        return elementName;
    }
}
