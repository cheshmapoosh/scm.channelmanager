package ir.daneshrefah.scm.cache.starter.utility.lock;

public class LockExecutionException extends RuntimeException {

    public LockExecutionException(String lockName, Throwable cause) {
        super("Could not execute locked job for lock '" + lockName + "'", cause);
    }
}
