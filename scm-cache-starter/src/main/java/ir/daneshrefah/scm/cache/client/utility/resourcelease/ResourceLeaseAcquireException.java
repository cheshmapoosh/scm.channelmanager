package ir.daneshrefah.scm.cache.client.utility.resourcelease;

public class ResourceLeaseAcquireException extends RuntimeException {

    public ResourceLeaseAcquireException(String message) {
        super(message);
    }

    public ResourceLeaseAcquireException(String message, Throwable cause) {
        super(message, cause);
    }
}
