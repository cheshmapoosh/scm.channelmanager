package ir.daneshrefah.scm.cache.client.utility.ratelimit;

public interface RateLimiterUtility {

    RateLimitResult tryConsume(String bucketName, String key);

    RateLimitResult tryConsume(String bucketName, String key, int tokenCountUsage);

    default RateLimitResult consumeOrThrow(String bucketName, String key) {
        return consumeOrThrow(bucketName, key, 1);
    }

    default RateLimitResult consumeOrThrow(String bucketName, String key, int tokenCountUsage) {
        RateLimitResult result = tryConsume(bucketName, key, tokenCountUsage);
        if (!result.allowed()) {
            throw new RateLimitExceededException(result);
        }
        return result;
    }
}
