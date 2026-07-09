package ir.daneshrefah.scm.cache.starter.utility.concurrencylimit;

import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
public class LocalConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {

    private static final String PROVIDER = "local";

    private final Map<String, Semaphore> limiters = new ConcurrentHashMap<>();
    private final ScmCacheEventSupport cacheEventSupport;

    public LocalConcurrencyLimiterUtility() {
        this(null);
    }

    public LocalConcurrencyLimiterUtility(ScmCacheEventSupport cacheEventSupport) {
        this.cacheEventSupport = cacheEventSupport;
    }

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
        long startedAt = System.nanoTime();
        try {
            boolean acquired = limiter(limitName, 1).tryAcquire();
            publishAcquireResult(limitName, startedAt, acquired, null);
            return acquired;
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", exception);
            throw exception;
        }
    }

    @Override
    public boolean tryAcquire(String limitName, int slots, Duration waitTime) {
        long startedAt = System.nanoTime();
        validateSlots(slots);
        Semaphore limiter = limiter(limitName, slots);
        try {
            boolean acquired;
            if (waitTime == null || waitTime.isNegative()) {
                limiter.acquire(slots);
                acquired = true;
            } else if (waitTime.isZero()) {
                acquired = limiter.tryAcquire(slots);
            } else {
                acquired = limiter.tryAcquire(slots, waitTime.toMillis(), TimeUnit.MILLISECONDS);
            }
            publishAcquireResult(limitName, startedAt, acquired, null);
            return acquired;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            RuntimeException wrapped = new ConcurrencyLimitOperationException("Interrupted while waiting to acquire local concurrency limiter '" + limitName + "'", exception);
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", wrapped);
            throw wrapped;
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", exception);
            throw exception;
        }
    }

    @Override
    public void release(String limitName) {
        long startedAt = System.nanoTime();
        try {
            limiter(limitName, 1).release();
            publish(ScmCacheEventType.CACHE_CONCURRENCY_RELEASED, limitName, startedAt, "released", null);
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", exception);
            throw exception;
        }
    }

    @Override
    public void release(String limitName, int slots) {
        long startedAt = System.nanoTime();
        validateSlots(slots);
        try {
            limiter(limitName, slots).release(slots);
            publish(ScmCacheEventType.CACHE_CONCURRENCY_RELEASED, limitName, startedAt, "released", null);
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", exception);
            throw exception;
        }
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
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, System.nanoTime(), "failure", exception);
            throw exception;
        } catch (Exception exception) {
            RuntimeException wrapped = new ConcurrencyLimitOperationException("Could not execute job for local concurrency limiter '" + limitName + "'", exception);
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, System.nanoTime(), "failure", wrapped);
            throw wrapped;
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

    private void publishAcquireResult(String limitName, long startedAt, boolean acquired, Throwable error) {
        publish(
                acquired ? ScmCacheEventType.CACHE_CONCURRENCY_ACQUIRED : ScmCacheEventType.CACHE_CONCURRENCY_REJECTED,
                limitName,
                startedAt,
                acquired ? "acquired" : "rejected",
                error
        );
    }

    private void publish(ScmCacheEventType type, String limitName, long startedAt, String result, Throwable error) {
        if (cacheEventSupport != null) {
            cacheEventSupport.concurrencyEvent(type, limitName, PROVIDER, startedAt, result, error);
        }
    }
}
