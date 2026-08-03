package ir.daneshrefah.scm.cache.starter.utility.resourcelease;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEventType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

@Slf4j
public class RoutingResourceLeaseUtility implements ResourceLeaseUtility {

    private final ResourceLeaseUtility localResourceLeaseUtility;
    private final ResourceLeaseUtility remoteResourceLeaseUtility;
    private final CacheClientProperties.UtilityBackends utilityBackends;
    private final ScmCacheEventSupport cacheEventSupport;

    public RoutingResourceLeaseUtility(ResourceLeaseUtility localResourceLeaseUtility,
                                       ResourceLeaseUtility remoteResourceLeaseUtility,
                                       CacheClientProperties.UtilityBackends utilityBackends) {
        this(localResourceLeaseUtility, remoteResourceLeaseUtility, utilityBackends, null);
    }

    public RoutingResourceLeaseUtility(ResourceLeaseUtility localResourceLeaseUtility,
                                       ResourceLeaseUtility remoteResourceLeaseUtility,
                                       CacheClientProperties.UtilityBackends utilityBackends,
                                       ScmCacheEventSupport cacheEventSupport) {
        this.localResourceLeaseUtility = Objects.requireNonNull(localResourceLeaseUtility, "localResourceLeaseUtility");
        this.remoteResourceLeaseUtility = remoteResourceLeaseUtility;
        this.utilityBackends = Objects.requireNonNull(utilityBackends, "utilityBackends");
        this.cacheEventSupport = cacheEventSupport;
    }

    @Override
    public ResourceLease acquire(String poolName, Collection<String> candidates, Duration ttl) {
        long startedAt = System.nanoTime();
        CacheClientProperties.UtilityBackendType backendType = utilityBackends.resolveResourceLease(poolName);
        String provider = provider(backendType);
        log.debug("Resource lease pool '{}' resolved to {} backend", poolName, backendType);
        try {
            ResourceLease lease = switch (backendType) {
                case LOCAL -> localResourceLeaseUtility.acquire(poolName, candidates, ttl);
                case REMOTE -> remoteResourceLeaseUtility().acquire(poolName, candidates, ttl);
            };
            publish(
                    ScmCacheEventType.CACHE_RESOURCE_LEASE_ACQUIRED,
                    poolName,
                    provider,
                    leaseKey(lease),
                    ttl,
                    startedAt,
                    "acquired",
                    null
            );
            return new PublishingResourceLease(lease, poolName, provider);
        } catch (ResourceLeaseAcquireException exception) {
            publish(
                    exception.getCause() == null
                            ? ScmCacheEventType.CACHE_RESOURCE_LEASE_REJECTED
                            : ScmCacheEventType.CACHE_RESOURCE_LEASE_ERROR,
                    poolName,
                    provider,
                    poolName,
                    ttl,
                    startedAt,
                    exception.getCause() == null ? "rejected" : "failure",
                    exception
            );
            throw exception;
        } catch (RuntimeException exception) {
            publish(ScmCacheEventType.CACHE_RESOURCE_LEASE_ERROR, poolName, provider, poolName, ttl, startedAt, "failure", exception);
            throw exception;
        }
    }

    private ResourceLeaseUtility remoteResourceLeaseUtility() {
        if (remoteResourceLeaseUtility == null) {
            throw new IllegalStateException("HazelcastInstance is required for REMOTE resource-lease backend");
        }
        return remoteResourceLeaseUtility;
    }

    private String provider(CacheClientProperties.UtilityBackendType backendType) {
        return backendType == CacheClientProperties.UtilityBackendType.LOCAL ? "local" : "remote";
    }

    private String leaseKey(ResourceLease lease) {
        return lease == null ? null : lease.poolName() + "::" + lease.resourceName();
    }

    private void publish(ScmCacheEventType type,
                         String poolName,
                         String provider,
                         Object leaseKey,
                         Duration ttl,
                         long startedAt,
                         String result,
                         Throwable error) {
        if (cacheEventSupport != null) {
            cacheEventSupport.resourceLeaseEvent(type, poolName, provider, leaseKey, ttl, startedAt, result, error);
        }
    }

    private final class PublishingResourceLease implements ResourceLease {
        private final ResourceLease delegate;
        private final String poolName;
        private final String provider;

        private PublishingResourceLease(ResourceLease delegate, String poolName, String provider) {
            this.delegate = delegate;
            this.poolName = poolName;
            this.provider = provider;
        }

        @Override
        public String poolName() {
            return delegate.poolName();
        }

        @Override
        public String resourceName() {
            return delegate.resourceName();
        }

        @Override
        public boolean isValid() {
            return delegate.isValid();
        }

        @Override
        public void onInvalidated(Runnable listener) {
            delegate.onInvalidated(listener);
        }

        @Override
        public void close() {
            long startedAt = System.nanoTime();
            try {
                delegate.close();
                publish(
                        ScmCacheEventType.CACHE_RESOURCE_LEASE_RELEASED,
                        poolName,
                        provider,
                        leaseKey(delegate),
                        null,
                        startedAt,
                        "released",
                        null
                );
            } catch (RuntimeException exception) {
                publish(
                        ScmCacheEventType.CACHE_RESOURCE_LEASE_ERROR,
                        poolName,
                        provider,
                        leaseKey(delegate),
                        null,
                        startedAt,
                        "failure",
                        exception
                );
                throw exception;
            }
        }
    }
}
