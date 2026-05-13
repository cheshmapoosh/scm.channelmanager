package ir.daneshrefah.scm.cache.client.utility.resourcelease;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;

@RequiredArgsConstructor
@Slf4j
public class RoutingResourceLeaseUtility implements ResourceLeaseUtility {

    private final ResourceLeaseUtility localResourceLeaseUtility;
    private final ResourceLeaseUtility remoteResourceLeaseUtility;
    private final CacheClientProperties.UtilityBackends utilityBackends;

    @Override
    public ResourceLease acquire(String poolName, Collection<String> candidates, Duration ttl) {
        CacheClientProperties.UtilityBackendType backendType = utilityBackends.resolveResourceLease(poolName);
        log.debug("Resource lease pool '{}' resolved to {} backend", poolName, backendType);
        return switch (backendType) {
            case LOCAL -> localResourceLeaseUtility.acquire(poolName, candidates, ttl);
            case REMOTE -> remoteResourceLeaseUtility().acquire(poolName, candidates, ttl);
        };
    }

    private ResourceLeaseUtility remoteResourceLeaseUtility() {
        if (remoteResourceLeaseUtility == null) {
            throw new IllegalStateException("HazelcastInstance is required for REMOTE resource-lease backend");
        }
        return remoteResourceLeaseUtility;
    }
}
