package ir.daneshrefah.scm.cache.client.utility.concurrencylimit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ISemaphore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class HazelcastConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public boolean initialize(String limitName, int maxConcurrentExecutions) {
        boolean initialized = semaphore(limitName).init(maxConcurrentExecutions);
        if (initialized) {
            log.info("Concurrency limiter '{}' initialized with {} slots", limitName, maxConcurrentExecutions);
        } else {
            log.debug("Concurrency limiter '{}' already initialized; init call ignored", limitName);
        }
        return initialized;
    }

    @Override
    public boolean tryAcquire(String limitName) {
        return semaphore(limitName).tryAcquire();
    }

    @Override
    public boolean tryAcquire(String limitName, int slots, Duration waitTime) {
        ISemaphore semaphore = semaphore(limitName);
        if (waitTime == null || waitTime.isNegative() || waitTime.isZero()) {
            return semaphore.tryAcquire(slots);
        }
        long millis = waitTime.toMillis();
        if (millis <= 0) {
            return semaphore.tryAcquire(slots);
        }
        try {
            return semaphore.tryAcquire(slots, millis, TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyLimitOperationException("Interrupted while waiting to acquire concurrency limiter '" + limitName + "'", exception);
        }
    }

    @Override
    public void release(String limitName) {
        semaphore(limitName).release();
    }

    @Override
    public void release(String limitName, int slots) {
        semaphore(limitName).release(slots);
    }

    @Override
    public int availableSlots(String limitName) {
        return semaphore(limitName).availablePermits();
    }

    @Override
    public <T> T executeWithConcurrencyLimit(String limitName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job) {
        ISemaphore semaphore = semaphore(limitName);
        if (maxConcurrentExecutions <= 0) {
            throw new IllegalArgumentException("maxConcurrentExecutions must be greater than zero");
        }
        boolean initialized = semaphore.init(maxConcurrentExecutions);
        if (initialized) {
            log.info("Concurrency limiter '{}' initialized with {} slots", limitName, maxConcurrentExecutions);
        }

        boolean acquired = acquireForExecution(limitName, semaphore, waitTime);
        if (!acquired) {
            log.warn("Could not acquire concurrency limiter: name='{}', waitTime={}", limitName, waitTime);
            throw new ConcurrencyLimitAcquireTimeoutException(limitName, waitTime);
        }
        log.debug("Concurrency limiter acquired: name='{}'", limitName);

        try {
            return job.call();
        } catch (Exception exception) {
            throw new ConcurrencyLimitOperationException("Could not execute job for concurrency limiter '" + limitName + "'", exception);
        } finally {
            semaphore.release();
            log.debug("Concurrency limiter released: name='{}'", limitName);
        }
    }

    private ISemaphore semaphore(String limitName) {
        return hazelcastInstance.getCPSubsystem().getSemaphore(limitName);
    }

    private boolean acquireForExecution(String limitName, ISemaphore semaphore, Duration waitTime) {
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
            throw new ConcurrencyLimitOperationException("Interrupted while waiting to acquire concurrency limiter '" + limitName + "'", exception);
        }
    }
}
