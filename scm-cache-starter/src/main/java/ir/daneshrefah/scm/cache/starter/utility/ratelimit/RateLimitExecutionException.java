package ir.daneshrefah.scm.cache.starter.utility.ratelimit;

public class RateLimitExecutionException extends RuntimeException {

    public RateLimitExecutionException(String bucketName, String key, Throwable cause) {
        super("Could not execute rate-limited job for bucket '" + bucketName + "', key '" + key + "'", cause);
    }
}
