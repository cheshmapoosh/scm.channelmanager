package ir.daneshrefah.scm.cache.starter.utility.ratelimit;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Function;

public interface RateLimiterUtility {

    RateLimitResult tryConsume(String bucketName, String key, int tokenCountUsage);

    default RateLimitResult tryConsume(String bucketName, String key) {
        return tryConsume(bucketName, key, 1);
    }

    default <T> T executeRateLimited(
            String bucketName,
            String key,
            int tokenCountUsage,
            Callable<T> job,
            Function<RateLimitExceededException, T> onFailure
    ) {
        Objects.requireNonNull(job, "job must not be null");
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        RateLimitResult result = tryConsume(bucketName, key, tokenCountUsage);
        if (!result.allowed()) {
            return onFailure.apply(new RateLimitExceededException(result));
        }
        try {
            return job.call();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RateLimitExecutionException(bucketName, key, exception);
        }
    }

    default <T> T executeRateLimited(
            String bucketName,
            String key,
            Callable<T> job,
            Function<RateLimitExceededException, T> onFailure
    ) {
        return executeRateLimited(bucketName, key, 1, job, onFailure);
    }
}
