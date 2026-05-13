package ir.daneshrefah.scm.cache.client.utility.lock;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Slf4j
public class HazelcastLockUtility implements LockUtility {

    private final HazelcastInstance hazelcastInstance;

    @Override
    public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(lockName);
        boolean acquired = acquire(lock, waitTime);
        if (!acquired) {
            log.warn("Could not acquire lock: lock='{}', waitTime={}", lockName, waitTime);
            throw new LockAcquireFailedException(lockName, waitTime);
        }
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
}
