package ir.daneshrefah.scm.cache.starter.config.exception;
/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdolahi
 * @version 1.0
 * @since 2023-11-22
 */
public class HazelCastClientInitializationException extends RuntimeException {

    public HazelCastClientInitializationException() {
        super("Hazelcast client initialization failed");
    }

    public HazelCastClientInitializationException(Throwable cause) {
        super("Hazelcast client initialization failed", cause);
    }
}
