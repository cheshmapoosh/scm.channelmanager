package ir.daneshrefah.scm.cache.starter.utility.concurrencylimit;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ConcurrencyLimiterUtility {

    boolean initialize(String limitName, int maxConcurrentExecutions);

    boolean tryAcquire(String limitName);

    boolean tryAcquire(String limitName, int slots, Duration waitTime);

    void release(String limitName);

    void release(String limitName, int slots);

    int availableSlots(String limitName);

    <T> T executeWithConcurrencyLimit(String limitName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job);

    default void runWithConcurrencyLimit(String limitName, int maxConcurrentExecutions, Duration waitTime, Runnable job) {
        executeWithConcurrencyLimit(limitName, maxConcurrentExecutions, waitTime, () -> {
            job.run();
            return null;
        });
    }

    default <T> T executeWithConcurrencyLimit(
            String limitName,
            int maxConcurrentExecutions,
            Duration waitTime,
            Callable<T> job,
            Function<RuntimeException, T> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            return executeWithConcurrencyLimit(limitName, maxConcurrentExecutions, waitTime, job);
        } catch (RuntimeException exception) {
            return onFailure.apply(exception);
        }
    }

    default void runWithConcurrencyLimit(
            String limitName,
            int maxConcurrentExecutions,
            Duration waitTime,
            Runnable job,
            Consumer<RuntimeException> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            runWithConcurrencyLimit(limitName, maxConcurrentExecutions, waitTime, job);
        } catch (RuntimeException exception) {
            onFailure.accept(exception);
        }
    }
}
