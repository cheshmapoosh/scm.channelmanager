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
            LockExecutionException wrapped =
                    new LockExecutionException(lockName, exception);
            primaryFailure = wrapped;
            log.error("Locked job was interrupted: lock='{}'", lockName, exception);
            throw wrapped;
        } catch (Exception exception) {
            LockExecutionException wrapped =
                    new LockExecutionException(lockName, exception);
            primaryFailure = wrapped;
            log.error("Could not execute locked job: lock='{}'", lockName, exception);
            throw wrapped;
        } catch (Error error) {
            primaryFailure = error;
            log.error("Error happened while executing locked job: lock='{}'", lockName, error);
            throw error;
        } finally {
            releaseLock(lock, lockName, primaryFailure);
        }
    }

    private void releaseLock(
            FencedLock lock,
            String lockName,
            Throwable primaryFailure
    ) {
        boolean ownershipVerified = false;
        try {
            if (!lock.isLockedByCurrentThread()) {
                throw new IllegalMonitorStateException(
                        "Current execution no longer owns the FencedLock"
                );
            }
            ownershipVerified = true;
            lock.unlock();
            log.debug("Lock '{}' released", lockName);
        } catch (Throwable cleanupFailure) {
            String cleanupPhase = ownershipVerified
                    ? "release lock"
                    : "verify lock ownership before release";
            LockExecutionException normalized = new LockExecutionException(
                    lockName,
                    cleanupPhase + (primaryFailure == null
                            ? " after protected operation completed"
                            : " after protected operation failed"),
                    cleanupFailure
            );
            if (primaryFailure == null) {
                log.error(
                        "Protected operation completed but lock cleanup failed: "
                                + "lock='{}', phase={}",
                        lockName,
                        cleanupPhase,
                        normalized
                );
                throw normalized;
            }
            if (primaryFailure != cleanupFailure
                    && primaryFailure != normalized) {
                primaryFailure.addSuppressed(normalized);
            }
            log.warn(
                    "Could not release lock after protected job failure: "
                            + "lock='{}', phase={}, outcome=suppressed",
                    lockName,
                    cleanupPhase,
                    normalized
            );
        }
    }

    private boolean acquire(FencedLock lock, Duration waitTime) {
        if (Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
            throw new LockExecutionException(
                    lock.getName(),
                    new InterruptedException(
                            "Interrupted before distributed lock acquisition"
                    )
            );
        }
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
