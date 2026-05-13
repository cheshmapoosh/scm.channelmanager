package ir.daneshrefah.scm.cache.client.utility.ratelimit;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

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

    default <T> T executeRateLimited(String bucketName, String key, Callable<T> job) {
        return executeRateLimited(bucketName, key, 1, job);
    }

    default <T> T executeRateLimited(String bucketName, String key, int tokenCountUsage, Callable<T> job) {
        consumeOrThrow(bucketName, key, tokenCountUsage);
        try {
            return job.call();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RateLimitExecutionException(bucketName, key, exception);
        }
    }

    default void runRateLimited(String bucketName, String key, Runnable job) {
        runRateLimited(bucketName, key, 1, job);
    }

    default void runRateLimited(String bucketName, String key, int tokenCountUsage, Runnable job) {
        executeRateLimited(bucketName, key, tokenCountUsage, () -> {
            job.run();
            return null;
        });
    }

    default <T> T executeRateLimited(
            String bucketName,
            String key,
            Callable<T> job,
            Function<RuntimeException, T> onFailure
    ) {
        return executeRateLimited(bucketName, key, 1, job, onFailure);
    }

    default <T> T executeRateLimited(
            String bucketName,
            String key,
            int tokenCountUsage,
            Callable<T> job,
            Function<RuntimeException, T> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            return executeRateLimited(bucketName, key, tokenCountUsage, job);
        } catch (RuntimeException exception) {
            return onFailure.apply(exception);
        }
    }

    default void runRateLimited(
            String bucketName,
            String key,
            Runnable job,
            Consumer<RuntimeException> onFailure
    ) {
        runRateLimited(bucketName, key, 1, job, onFailure);
    }

    default void runRateLimited(
            String bucketName,
            String key,
            int tokenCountUsage,
            Runnable job,
            Consumer<RuntimeException> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            runRateLimited(bucketName, key, tokenCountUsage, job);
        } catch (RuntimeException exception) {
            onFailure.accept(exception);
        }
    }
}
