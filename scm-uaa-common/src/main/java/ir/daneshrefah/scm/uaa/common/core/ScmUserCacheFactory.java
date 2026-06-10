package ir.daneshrefah.scm.uaa.common.core;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.util.StringUtils;

public final class ScmUserCacheFactory {
    private static final String DEFAULT_CACHE_NAME = "user_cache";

    private ScmUserCacheFactory() {
    }

    public static UserCache create(CacheManager cacheManager, String cacheName) {
        return create(cacheManager, cacheName, null);
    }

    public static UserCache create(
            CacheManager cacheManager,
            String cacheName,
            String keyExpression
    ) {
        String resolvedCacheName = StringUtils.hasText(cacheName) ? cacheName.trim() : DEFAULT_CACHE_NAME;
        Cache cache = cacheManager.getCache(resolvedCacheName);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + resolvedCacheName);
        }
        return new SpringCacheBasedUserCache(new UserDetailsKeyResolvingCache(cache, keyExpression));
    }
}
