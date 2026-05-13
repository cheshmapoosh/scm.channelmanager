package ir.daneshrefah.scm.cache.client.utility.lock;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
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

    @Override
    public <T> T executeWithLock(String lockName, Duration waitTime, Callable<T> job) {
        CacheClientProperties.UtilityBackendType backendType = utilityBackends.resolveLock(lockName);
        log.debug("Lock '{}' resolved to {} backend", lockName, backendType);
        return switch (backendType) {
            case LOCAL -> localLockUtility.executeWithLock(lockName, waitTime, job);
            case REMOTE -> remoteLockUtility().executeWithLock(lockName, waitTime, job);
        };
    }

    private LockUtility remoteLockUtility() {
        if (remoteLockUtility == null) {
            throw new IllegalStateException("HazelcastInstance is required for REMOTE lock backend");
        }
        return remoteLockUtility;
    }
}
