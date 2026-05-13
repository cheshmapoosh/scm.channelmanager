package ir.daneshrefah.scm.cache.client.utility.semaphore;

import java.time.Duration;
import java.util.concurrent.Callable;

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
}
