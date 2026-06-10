package ir.daneshrefah.scm.cache.client.utility.concurrencylimit;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LocalConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {

    private final Map<String, Semaphore> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean initialize(String limitName, int maxConcurrentExecutions) {
        validateSlots(maxConcurrentExecutions);
        Semaphore previous = limiters.putIfAbsent(limitName, new Semaphore(maxConcurrentExecutions, true));
        boolean initialized = previous == null;
        if (initialized) {
            log.info("Local concurrency limiter '{}' initialized with {} slots", limitName, maxConcurrentExecutions);
        } else {
            log.debug("Local concurrency limiter '{}' already initialized; init call ignored", limitName);
        }
        return initialized;
    }

    @Override
    public boolean tryAcquire(String limitName) {
        return limiter(limitName, 1).tryAcquire();
    }

    @Override
    public boolean tryAcquire(String limitName, int slots, Duration waitTime) {
        validateSlots(slots);
        Semaphore limiter = limiter(limitName, slots);
        try {
            if (waitTime == null || waitTime.isNegative()) {
                limiter.acquire(slots);
                return true;
            }
            if (waitTime.isZero()) {
                return limiter.tryAcquire(slots);
            }
            return limiter.tryAcquire(slots, waitTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ConcurrencyLimitOperationException("Interrupted while waiting to acquire local concurrency limiter '" + limitName + "'", exception);
        }
    }

    @Override
    public void release(String limitName) {
        limiter(limitName, 1).release();
    }

    @Override
    public void release(String limitName, int slots) {
        validateSlots(slots);
        limiter(limitName, slots).release(slots);
    }

    @Override
    public int availableSlots(String limitName) {
        return limiter(limitName, 1).availablePermits();
    }

    @Override
    public <T> T executeWithConcurrencyLimit(String limitName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job) {
        validateSlots(maxConcurrentExecutions);
        initialize(limitName, maxConcurrentExecutions);
        boolean acquired = tryAcquire(limitName, 1, waitTime);
        if (!acquired) {
            log.warn("Could not acquire local concurrency limiter: name='{}', waitTime={}", limitName, waitTime);
            throw new ConcurrencyLimitAcquireTimeoutException(limitName, waitTime);
        }
        log.debug("Local concurrency limiter acquired: name='{}'", limitName);
        try {
            return job.call();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ConcurrencyLimitOperationException("Could not execute job for local concurrency limiter '" + limitName + "'", exception);
        } finally {
            release(limitName);
            log.debug("Local concurrency limiter released: name='{}'", limitName);
        }
    }

    private Semaphore limiter(String limitName, int slots) {
        return limiters.computeIfAbsent(limitName, ignored -> new Semaphore(slots, true));
    }

    private void validateSlots(int slots) {
        if (slots <= 0) {
            throw new IllegalArgumentException("slots must be greater than zero");
        }
    }
}
