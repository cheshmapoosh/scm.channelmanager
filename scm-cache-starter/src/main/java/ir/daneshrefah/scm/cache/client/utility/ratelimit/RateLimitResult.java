package ir.daneshrefah.scm.cache.client.utility.ratelimit;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public record RateLimitResult(
        String bucketName,
        String key,
        int requestedTokens,
        boolean allowed,
        boolean bucketConfigured,
        long remainingTokens,
        long nanosToWaitForRefill,
        long nanosToWaitForReset
) {

    public long retryAfterSeconds() {
        if (nanosToWaitForRefill <= 0) {
            return 0;
        }
        long seconds = TimeUnit.NANOSECONDS.toSeconds(nanosToWaitForRefill);
        return seconds > 0 ? seconds : 1;
    }

    public Map<String, Object> toHeaders() {
        Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(RateLimitHeaderNames.RATE_LIMIT_ALLOWED, allowed);
        headers.put(RateLimitHeaderNames.RATE_LIMIT_REMAINING, remainingTokens);
        headers.put(RateLimitHeaderNames.RATE_LIMIT_RETRY_AFTER_SECONDS, retryAfterSeconds());
        headers.put(RateLimitHeaderNames.RATE_LIMIT_BUCKET, bucketName);
        headers.put(RateLimitHeaderNames.RATE_LIMIT_KEY, key);
        headers.put(RateLimitHeaderNames.RATE_LIMIT_REQUESTED_TOKENS, requestedTokens);
        return headers;
    }

    public static RateLimitResult missingBucket(String bucketName, String key, int requestedTokens, boolean allowWhenMissing) {
        return new RateLimitResult(
                bucketName,
                key,
                requestedTokens,
                allowWhenMissing,
                false,
                -1,
                0,
                0
        );
    }
}
