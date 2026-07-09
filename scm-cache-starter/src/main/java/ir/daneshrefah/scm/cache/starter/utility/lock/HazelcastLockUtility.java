package ir.daneshrefah.scm.cache.starter.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
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
        long startedAt = System.nanoTime();
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(lockName);
        boolean acquired = acquire(lock, waitTime);
        if (!acquired) {
            log.warn("Could not acquire lock: lock='{}', waitTime={}", lockName, waitTime);
            throw new LockAcquireFailedException(lockName, waitTime);
        }
        publish(ScmCacheEventType.CACHE_LOCK_ACQUIRED, lockName, startedAt, "acquired", null);
        log.debug("Lock acquired: lock='{}'", lockName);
        try {
            return job.call();
        } catch (RuntimeException exception) {
            log.error("Runtime exception happened while executing locked job: lock='{}'", lockName, exception);
            throw exception;
        } catch (Exception exception) {
            log.error("Could not execute locked job: lock='{}'", lockName, exception);
            throw new LockExecutionException(lockName, exception);
        } finally {
            lock.unlock();
            log.debug("Lock '{}' released", lockName);
        }
    }

    private boolean acquire(FencedLock lock, Duration waitTime) {
        if (waitTime == null || waitTime.isNegative()) {
            lock.lock();
            return true;
        }
        if (waitTime.isZero()) {
            return lock.tryLock();
        }
        long millis = waitTime.toMillis();
        if (millis <= 0) {
            return lock.tryLock();
        }
        return lock.tryLock(millis, TimeUnit.MILLISECONDS);
    }

    private void publish(ScmCacheEventType type, String lockName, long startedAt, String result, Throwable error) {
        if (cacheEventSupport != null) {
            cacheEventSupport.lockEvent(type, lockName, PROVIDER, startedAt, result, error);
        }
    }
}
