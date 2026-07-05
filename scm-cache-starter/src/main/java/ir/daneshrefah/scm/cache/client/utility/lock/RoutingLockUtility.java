package ir.daneshrefah.scm.cache.client.utility.lock;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Slf4j
public class RoutingLockUtility implements LockUtility {

    private final LockUtility localLockUtility;
    private final LockUtility remoteLockUtility;
    private final CacheClientProperties.UtilityBackends utilityBackends;
    private final ScmCacheEventSupport cacheEventSupport;

    @Override
    public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
        long startedAt = System.nanoTime();
        CacheClientProperties.UtilityBackendType backendType = utilityBackends.resolveLock(lockName);
        String provider = provider(backendType);
        log.debug("Lock '{}' resolved to {} backend", lockName, backendType);
        try {
            T result = switch (backendType) {
                case LOCAL -> localLockUtility.executeWithLock(lockName, waitTime, job);
                case REMOTE -> remoteLockUtility().executeWithLock(lockName, waitTime, job);
            };
            cacheEventSupport.lockEvent(ScmCacheEventType.CACHE_LOCK_COMPLETED, lockName, provider, startedAt, "completed", null);
            return result;
        } catch (LockAcquireFailedException exception) {
            cacheEventSupport.lockEvent(ScmCacheEventType.CACHE_LOCK_FAILED, lockName, provider, startedAt, "failure", exception);
            throw exception;
        } catch (LockExecutionException exception) {
            cacheEventSupport.lockEvent(ScmCacheEventType.CACHE_LOCK_EXECUTION_FAILED, lockName, provider, startedAt, "failure", exception);
            throw exception;
        } catch (RuntimeException exception) {
            cacheEventSupport.lockEvent(ScmCacheEventType.CACHE_ERROR, lockName, provider, startedAt, "failure", exception);
            throw exception;
        }
    }

    private String provider(CacheClientProperties.UtilityBackendType backendType) {
        return backendType == CacheClientProperties.UtilityBackendType.LOCAL ? "local" : "remote";
    }

    private LockUtility remoteLockUtility() {
        if (remoteLockUtility == null) {
            throw new IllegalStateException("HazelcastInstance is required for REMOTE lock backend");
        }
        return remoteLockUtility;
    }
}
