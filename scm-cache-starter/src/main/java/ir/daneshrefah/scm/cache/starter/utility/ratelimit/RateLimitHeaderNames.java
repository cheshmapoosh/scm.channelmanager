package ir.daneshrefah.scm.cache.starter.utility.ratelimit;

public final class RateLimitHeaderNames {

    public static final String RATE_LIMIT_ALLOWED = "X-Rate-Limit-Allowed";
    public static final String RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    public static final String RATE_LIMIT_RETRY_AFTER_SECONDS = "X-Rate-Limit-Retry-After-Seconds";
    public static final String RATE_LIMIT_BUCKET = "X-Rate-Limit-Bucket";
    public static final String RATE_LIMIT_KEY = "X-Rate-Limit-Key";
    public static final String RATE_LIMIT_REQUESTED_TOKENS = "X-Rate-Limit-Requested-Tokens";

    private RateLimitHeaderNames() {
    }
}
