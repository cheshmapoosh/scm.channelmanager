package ir.daneshrefah.scm.cache.starter.utility.ratelimit;

public class RateLimitExceededException extends RuntimeException {

    private final RateLimitResult result;

    public RateLimitExceededException(RateLimitResult result) {
        super("Rate limit exceeded for bucket '" + result.bucketName() + "', key '" + result.key() +
                "', retryAfterSeconds=" + result.retryAfterSeconds());
        this.result = result;
    }

    public RateLimitResult getResult() {
        return result;
    }
}
