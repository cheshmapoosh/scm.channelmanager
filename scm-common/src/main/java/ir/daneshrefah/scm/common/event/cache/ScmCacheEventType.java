package ir.daneshrefah.scm.common.event.cache;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmCacheEventType implements ScmEventType {
    CACHE_GET("cache.get"),
    CACHE_HIT("cache.hit"),
    CACHE_MISS("cache.miss"),
    CACHE_PUT("cache.put"),
    CACHE_EVICT("cache.evict"),
    CACHE_CLEAR("cache.clear"),
    CACHE_ERROR("cache.error"),
    CACHE_LOCK_ACQUIRED("cache.lock.acquired"),
    CACHE_LOCK_FAILED("cache.lock.failed"),
    CACHE_LOCK_COMPLETED("cache.lock.completed"),
    CACHE_LOCK_EXECUTION_FAILED("cache.lock.execution.failed"),
    CACHE_RATE_LIMIT_CONSUMED("cache.rate_limit.consumed"),
    CACHE_RATE_LIMIT_REJECTED("cache.rate_limit.rejected"),
    CACHE_CONCURRENCY_ACQUIRED("cache.concurrency.acquired"),
    CACHE_CONCURRENCY_REJECTED("cache.concurrency.rejected"),
    CACHE_CONCURRENCY_RELEASED("cache.concurrency.released"),
    CACHE_CONCURRENCY_ERROR("cache.concurrency.error"),
    CACHE_RESOURCE_LEASE_ACQUIRED("cache.resource_lease.acquired"),
    CACHE_RESOURCE_LEASE_REJECTED("cache.resource_lease.rejected"),
    CACHE_RESOURCE_LEASE_RELEASED("cache.resource_lease.released"),
    CACHE_RESOURCE_LEASE_ERROR("cache.resource_lease.error"),
    CACHE_QUEUE_OFFERED("cache.queue.offered"),
    CACHE_QUEUE_POLLED("cache.queue.polled"),
    CACHE_QUEUE_ERROR("cache.queue.error");

    private final String code;

    ScmCacheEventType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
