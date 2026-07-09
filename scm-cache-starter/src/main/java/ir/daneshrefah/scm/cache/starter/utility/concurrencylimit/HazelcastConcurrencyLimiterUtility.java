package ir.daneshrefah.scm.cache.starter.utility.concurrencylimit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ISemaphore;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HazelcastConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {

    private static final String PROVIDER = "remote";

    private final HazelcastInstance hazelcastInstance;
    private final ScmCacheEventSupport cacheEventSupport;

    public HazelcastConcurrencyLimiterUtility(HazelcastInstance hazelcastInstance) {
        this(hazelcastInstance, null);
    }

    public HazelcastConcurrencyLimiterUtility(HazelcastInstance hazelcastInstance, ScmCacheEventSupport cacheEventSupport) {
        this.hazelcastInstance = hazelcastInstance;
        this.cacheEventSupport = cacheEventSupport;
    }

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
        long startedAt = System.nanoTime();
        try {
            boolean acquired = semaphore(limitName).tryAcquire();
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
        ISemaphore semaphore = semaphore(limitName);
        try {
            boolean acquired;
            if (waitTime == null || waitTime.isNegative() || waitTime.isZero()) {
                acquired = semaphore.tryAcquire(slots);
            } else {
                long millis = waitTime.toMillis();
                acquired = millis <= 0
                        ? semaphore.tryAcquire(slots)
                        : semaphore.tryAcquire(slots, millis, TimeUnit.MILLISECONDS);
            }
            publishAcquireResult(limitName, startedAt, acquired, null);
            return acquired;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            RuntimeException wrapped = new ConcurrencyLimitOperationException("Interrupted while waiting to acquire concurrency limiter '" + limitName + "'", exception);
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
            semaphore(limitName).release();
            publish(ScmCacheEventType.CACHE_CONCURRENCY_RELEASED, limitName, startedAt, "released", null);
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", exception);
            throw exception;
        }
    }

    @Override
    public void release(String limitName, int slots) {
        long startedAt = System.nanoTime();
        try {
            semaphore(limitName).release(slots);
            publish(ScmCacheEventType.CACHE_CONCURRENCY_RELEASED, limitName, startedAt, "released", null);
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", exception);
            throw exception;
        }
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
            RuntimeException wrapped = exception instanceof RuntimeException runtimeException
                    ? runtimeException
                    : new ConcurrencyLimitOperationException("Could not execute job for concurrency limiter '" + limitName + "'", exception);
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, System.nanoTime(), "failure", wrapped);
            throw wrapped;
        } finally {
            semaphore.release();
            publish(ScmCacheEventType.CACHE_CONCURRENCY_RELEASED, limitName, System.nanoTime(), "released", null);
            log.debug("Concurrency limiter released: name='{}'", limitName);
        }
    }

    private ISemaphore semaphore(String limitName) {
        return hazelcastInstance.getCPSubsystem().getSemaphore(limitName);
    }

    private boolean acquireForExecution(String limitName, ISemaphore semaphore, Duration waitTime) {
        long startedAt = System.nanoTime();
        try {
            boolean acquired;
            if (waitTime == null || waitTime.isNegative()) {
                semaphore.acquire();
                acquired = true;
            } else if (waitTime.isZero()) {
                acquired = semaphore.tryAcquire();
            } else {
                long millis = waitTime.toMillis();
                acquired = millis <= 0
                        ? semaphore.tryAcquire()
                        : semaphore.tryAcquire(1, millis, TimeUnit.MILLISECONDS);
            }
            publishAcquireResult(limitName, startedAt, acquired, null);
            return acquired;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            RuntimeException wrapped = new ConcurrencyLimitOperationException("Interrupted while waiting to acquire concurrency limiter '" + limitName + "'", exception);
            publish(ScmCacheEventType.CACHE_CONCURRENCY_ERROR, limitName, startedAt, "failure", wrapped);
            throw wrapped;
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
