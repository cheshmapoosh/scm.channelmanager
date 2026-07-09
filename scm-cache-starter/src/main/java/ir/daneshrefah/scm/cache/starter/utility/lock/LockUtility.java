package ir.daneshrefah.scm.cache.starter.utility.lock;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface LockUtility {

    <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job);

    /**
     * Blocking lock mode (same behavior style as concurrency-limit queued mode):
     * waits until lock is acquired.
     */
    default <T> T executeWithLock(String lockName, boolean block, Callable<T> job) {
        return executeWithLock(lockName, block ? null : Duration.ZERO, job);
    }

    default <T> T executeWithLock(String lockName, Callable<T> job) {
        return executeWithLock(lockName, Duration.ZERO, job);
    }

    default void runWithLock(String lockName, Duration waitTime, Runnable job) {
        executeWithLock(lockName, waitTime, () -> {
            job.run();
            return null;
        });
    }

    default void runWithLock(String lockName, Runnable job) {
        runWithLock(lockName, Duration.ZERO, job);
    }

    default void runWithLock(String lockName, boolean block, Runnable job) {
        executeWithLock(lockName, block, () -> {
            job.run();
            return null;
        });
    }

    default <T> T executeWithLock(
            String lockName,
            Duration waitTime,
            Callable<T> job,
            Function<RuntimeException, T> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            return executeWithLock(lockName, waitTime, job);
        } catch (RuntimeException exception) {
            return onFailure.apply(exception);
        }
    }

    default <T> T executeWithLock(
            String lockName,
            boolean block,
            Callable<T> job,
            Function<RuntimeException, T> onFailure
    ) {
        return executeWithLock(lockName, block ? null : Duration.ZERO, job, onFailure);
    }

    default void runWithLock(
            String lockName,
            Duration waitTime,
            Runnable job,
            Consumer<RuntimeException> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            runWithLock(lockName, waitTime, job);
        } catch (RuntimeException exception) {
            onFailure.accept(exception);
        }
    }

    default void runWithLock(
            String lockName,
            boolean block,
            Runnable job,
            Consumer<RuntimeException> onFailure
    ) {
        runWithLock(lockName, block ? null : Duration.ZERO, job, onFailure);
    }
}
