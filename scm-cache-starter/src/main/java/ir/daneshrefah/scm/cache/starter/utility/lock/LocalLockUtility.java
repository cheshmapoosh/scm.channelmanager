package ir.daneshrefah.scm.cache.starter.utility.lock;

import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class LocalLockUtility implements LockUtility {

    private static final String PROVIDER = "local";

    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final ScmCacheEventSupport cacheEventSupport;

    public LocalLockUtility() {
        this(null);
    }

    public LocalLockUtility(ScmCacheEventSupport cacheEventSupport) {
        this.cacheEventSupport = cacheEventSupport;
    }

    @Override
    public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
        long startedAt = System.nanoTime();
        ReentrantLock lock = locks.computeIfAbsent(lockName, ignored -> new ReentrantLock());
        boolean acquired = acquire(lockName, lock, waitTime);
        if (!acquired) {
            log.warn("Could not acquire local lock: lock='{}', waitTime={}", lockName, waitTime);
            throw new LockAcquireFailedException(lockName, waitTime);
        }
        publish(ScmCacheEventType.CACHE_LOCK_ACQUIRED, lockName, startedAt, "acquired", null);
        log.debug("Local lock acquired: lock='{}'", lockName);
        try {
            return job.call();
        } catch (RuntimeException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new LockExecutionException(lockName, exception);
        } finally {
            lock.unlock();
            log.debug("Local lock '{}' released", lockName);
        }
    }

    private boolean acquire(String lockName, ReentrantLock lock, Duration waitTime) {
        if (waitTime == null || waitTime.isNegative()) {
            lock.lock();
            return true;
        }
        if (waitTime.isZero()) {
            return lock.tryLock();
        }
        try {
            return lock.tryLock(waitTime.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new LockExecutionException(lockName, exception);
        }
    }

    private void publish(ScmCacheEventType type, String lockName, long startedAt, String result, Throwable error) {
        if (cacheEventSupport != null) {
            cacheEventSupport.lockEvent(type, lockName, PROVIDER, startedAt, result, error);
        }
    }
}
