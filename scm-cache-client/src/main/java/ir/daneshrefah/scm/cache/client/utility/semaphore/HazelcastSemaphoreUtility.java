package ir.daneshrefah.scm.cache.client.utility.semaphore;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ISemaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class HazelcastSemaphoreUtility implements SemaphoreUtility {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public boolean initialize(String semaphoreName, int permits) {
        boolean initialized = semaphore(semaphoreName).init(permits);
        if (initialized) {
            log.info("Semaphore '{}' initialized with {} permits", semaphoreName, permits);
        } else {
            log.debug("Semaphore '{}' already initialized; init call ignored", semaphoreName);
        }
        return initialized;
    }

    @Override
    public boolean tryAcquire(String semaphoreName) {
        return semaphore(semaphoreName).tryAcquire();
    }

    @Override
    public boolean tryAcquire(String semaphoreName, int permits, Duration waitTime) {
        ISemaphore semaphore = semaphore(semaphoreName);
        if (waitTime == null || waitTime.isNegative() || waitTime.isZero()) {
            return semaphore.tryAcquire(permits);
        }
        long millis = waitTime.toMillis();
        if (millis <= 0) {
            return semaphore.tryAcquire(permits);
        }
        try {
            return semaphore.tryAcquire(permits, millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SemaphoreOperationException("Interrupted while waiting to acquire semaphore '" + semaphoreName + "'", exception);
        }
    }

    @Override
    public void release(String semaphoreName) {
        semaphore(semaphoreName).release();
    }

    @Override
    public void release(String semaphoreName, int permits) {
        semaphore(semaphoreName).release(permits);
    }

    @Override
    public int availablePermits(String semaphoreName) {
        return semaphore(semaphoreName).availablePermits();
    }

    @Override
    public <T> T executeQueued(String semaphoreName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job) {
        ISemaphore semaphore = semaphore(semaphoreName);
        if (maxConcurrentExecutions <= 0) {
            throw new IllegalArgumentException("maxConcurrentExecutions must be greater than zero");
        }
        boolean initialized = semaphore.init(maxConcurrentExecutions);
        if (initialized) {
            log.info("Semaphore '{}' initialized with {} permits", semaphoreName, maxConcurrentExecutions);
        }

        boolean acquired = acquireForQueuedExecution(semaphoreName, semaphore, waitTime);
        if (!acquired) {
            log.warn("Could not acquire semaphore for queued execution: semaphore='{}', waitTime={}", semaphoreName, waitTime);
            throw new SemaphoreAcquireTimeoutException(semaphoreName, waitTime);
        }
        log.debug("Semaphore acquired for queued execution: semaphore='{}'", semaphoreName);

        try {
            return job.call();
        } catch (Exception exception) {
            throw new SemaphoreOperationException("Could not execute queued job for semaphore '" + semaphoreName + "'", exception);
        } finally {
            semaphore.release();
            log.debug("Semaphore released for queued execution: semaphore='{}'", semaphoreName);
        }
    }

    private ISemaphore semaphore(String semaphoreName) {
        return hazelcastInstance.getCPSubsystem().getSemaphore(semaphoreName);
    }

    private boolean acquireForQueuedExecution(String semaphoreName, ISemaphore semaphore, Duration waitTime) {
        try {
            if (waitTime == null || waitTime.isNegative()) {
                semaphore.acquire();
                return true;
            }
            if (waitTime.isZero()) {
                return semaphore.tryAcquire();
            }
            long millis = waitTime.toMillis();
            if (millis <= 0) {
                return semaphore.tryAcquire();
            }
            return semaphore.tryAcquire(1, millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new SemaphoreOperationException("Interrupted while waiting to acquire semaphore '" + semaphoreName + "'", exception);
        }
    }
}
