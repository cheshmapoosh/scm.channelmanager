package ir.daneshrefah.scm.cache.starter.utility.concurrencylimit;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Slf4j
public class RoutingConcurrencyLimiterUtility implements ConcurrencyLimiterUtility {

    private final ConcurrencyLimiterUtility localConcurrencyLimiterUtility;
    private final ConcurrencyLimiterUtility remoteConcurrencyLimiterUtility;
    private final CacheClientProperties.UtilityBackends utilityBackends;

    @Override
    public boolean initialize(String limitName, int maxConcurrentExecutions) {
        return delegate(limitName).initialize(limitName, maxConcurrentExecutions);
    }

    @Override
    public boolean tryAcquire(String limitName) {
        return delegate(limitName).tryAcquire(limitName);
    }

    @Override
    public boolean tryAcquire(String limitName, int slots, Duration waitTime) {
        return delegate(limitName).tryAcquire(limitName, slots, waitTime);
    }

    @Override
    public void release(String limitName) {
        delegate(limitName).release(limitName);
    }

    @Override
    public void release(String limitName, int slots) {
        delegate(limitName).release(limitName, slots);
    }

    @Override
    public int availableSlots(String limitName) {
        return delegate(limitName).availableSlots(limitName);
    }

    @Override
    public <T> T executeWithConcurrencyLimit(String limitName, int maxConcurrentExecutions, Duration waitTime, Callable<T> job) {
        return delegate(limitName).executeWithConcurrencyLimit(limitName, maxConcurrentExecutions, waitTime, job);
    }

    private ConcurrencyLimiterUtility delegate(String limitName) {
        CacheClientProperties.UtilityBackendType backendType = utilityBackends.resolveConcurrencyLimit(limitName);
        log.debug("Concurrency limit '{}' resolved to {} backend", limitName, backendType);
        return switch (backendType) {
            case LOCAL -> localConcurrencyLimiterUtility;
            case REMOTE -> remoteConcurrencyLimiterUtility();
        };
    }

    private ConcurrencyLimiterUtility remoteConcurrencyLimiterUtility() {
        if (remoteConcurrencyLimiterUtility == null) {
            throw new IllegalStateException("HazelcastInstance is required for REMOTE concurrency-limit backend");
        }
        return remoteConcurrencyLimiterUtility;
    }
}
