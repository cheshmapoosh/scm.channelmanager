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
    CACHE_RATE_LIMIT_CONSUMED("cache.rate_limit.consumed"),
    CACHE_RATE_LIMIT_REJECTED("cache.rate_limit.rejected");

    private final String code;

    ScmCacheEventType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
