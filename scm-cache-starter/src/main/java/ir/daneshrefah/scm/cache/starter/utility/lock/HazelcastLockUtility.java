package ir.daneshrefah.scm.cache.starter.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.map.IMap;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HazelcastLockUtility implements LockUtility {

    private static final String PROVIDER = "remote";

    private final HazelcastInstance hazelcastInstance;
    private final ScmCacheEventSupport cacheEventSupport;

    public HazelcastLockUtility(HazelcastInstance hazelcastInstance) {
        this(hazelcastInstance, null);
    }

    public HazelcastLockUtility(HazelcastInstance hazelcastInstance, ScmCacheEventSupport cacheEventSupport) {
        this.hazelcastInstance = hazelcastInstance;
        this.cacheEventSupport = cacheEventSupport;
    }

    @Override


    public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
        Objects.requireNonNull(lockName, "lockName must not be null");
        Objects.requireNonNull(waitTime, "waitTime must not be null");
        Objects.requireNonNull(job, "job must not be null");

        if (lockName.isBlank()) {
            throw new IllegalArgumentException("lockName must not be blank");
        }
        if (waitTime.isNegative()) {
            throw new IllegalArgumentException("waitTime must not be negative");
        }

        long startedAt = System.nanoTime();
        String lockKey = "lock:" + lockName;
        IMap<String, Boolean> locks = hazelcastInstance.getMap("distributed-locks");

        boolean acquired = acquire(locks, lockKey, waitTime, Duration.ofMinutes(5));
        if (!acquired) {
            log.warn("Could not acquire lock: lock='{}', waitTime={}", lockName, waitTime);
            throw new LockAcquireFailedException(lockName, waitTime);
        }

        publish(ScmCacheEventType.CACHE_LOCK_ACQUIRED, lockName, startedAt, "acquired", null);
        log.debug("Lock acquired: lock='{}'", lockName);

        Throwable primaryFailure = null;
        try {
            return job.call();
        } catch (RuntimeException exception) {
            primaryFailure = exception;
            restoreInterrupt(exception);
            log.error("Runtime exception happened while executing locked job: lock='{}'", lockName, exception);
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            LockExecutionException wrapped = new LockExecutionException(lockName, exception);
            primaryFailure = wrapped;
            log.error("Locked job was interrupted: lock='{}'", lockName, exception);
            throw wrapped;
        } catch (Exception exception) {
            LockExecutionException wrapped = new LockExecutionException(lockName, exception);
            primaryFailure = wrapped;
            log.error("Could not execute locked job: lock='{}'", lockName, exception);
            throw wrapped;
        } catch (Error error) {
            primaryFailure = error;
            log.error("Error happened while executing locked job: lock='{}'", lockName, error);
            throw error;
        } finally {
            releaseLock(locks, lockKey, lockName, primaryFailure, startedAt);
        }
    }

    private boolean acquire(IMap<String, Boolean> locks, String lockKey, Duration waitTime, Duration leaseTime) {
        try {
            return locks.tryLock(
                    lockKey,
                    waitTime.toMillis(), TimeUnit.MILLISECONDS,
                    leaseTime.toMillis(), TimeUnit.MILLISECONDS
            );
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while acquiring lock: lockKey='{}', waitTime={}, leaseTime={}",
                    lockKey, waitTime, leaseTime, exception);
            return false;
        }
    }

    private void releaseLock(IMap<String, Boolean> locks,
                             String lockKey,
                             String lockName,
                             Throwable primaryFailure,
                             long startedAt) {
        try {
            locks.unlock(lockKey);
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
            log.debug("Lock released: lock='{}', heldFor={}ms", lockName, elapsedMillis);
        } catch (RuntimeException exception) {
            if (primaryFailure != null) {
                primaryFailure.addSuppressed(exception);
            }
            log.error("Could not release lock: lock='{}'", lockName, exception);
        }
    }


    private void restoreInterrupt(Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof InterruptedException) {
                Thread.currentThread().interrupt();
                return;
            }
            current = current.getCause();
        }
    }

    private void publish(ScmCacheEventType type, String lockName, long startedAt, String result, Throwable error) {
        if (cacheEventSupport != null) {
            cacheEventSupport.lockEvent(type, lockName, PROVIDER, startedAt, result, error);
        }
    }
}
