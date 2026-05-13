package ir.daneshrefah.scm.cache.client.utility.semaphore;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SemaphoreUtility {

    boolean initialize(String semaphoreName, int permits);

    boolean tryAcquire(String semaphoreName);

    boolean tryAcquire(String semaphoreName, int permits, Duration waitTime);

    void release(String semaphoreName);

    void release(String semaphoreName, int permits);

    int availablePermits(String semaphoreName);

    <T> T executeQueued(String semaphoreName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job);

    default void runQueued(String semaphoreName, int maxConcurrentExecutions, Duration waitTime, Runnable job) {
        executeQueued(semaphoreName, maxConcurrentExecutions, waitTime, () -> {
            job.run();
            return null;
        });
    }

    default <T> T executeQueued(
            String semaphoreName,
            int maxConcurrentExecutions,
            Duration waitTime,
            Callable<T> job,
            Function<RuntimeException, T> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            return executeQueued(semaphoreName, maxConcurrentExecutions, waitTime, job);
        } catch (RuntimeException exception) {
            return onFailure.apply(exception);
        }
    }

    default void runQueued(
            String semaphoreName,
            int maxConcurrentExecutions,
            Duration waitTime,
            Runnable job,
            Consumer<RuntimeException> onFailure
    ) {
        Objects.requireNonNull(onFailure, "onFailure must not be null");
        try {
            runQueued(semaphoreName, maxConcurrentExecutions, waitTime, job);
        } catch (RuntimeException exception) {
            onFailure.accept(exception);
        }
    }
}
