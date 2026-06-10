package ir.daneshrefah.scm.uaa.config;

import ir.daneshrefah.scm.uaa.common.core.ScmUserCacheFactory;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserCache;

@Configuration(proxyBeanMethods = false)
public class UaaLocalCacheConfiguration {
    private static final String SESSION_CACHE_NAME = "session_cache";
    private static final String USER_CACHE_NAME = "user_cache";

    @Bean
    public SessionCache sessionCache(CacheManager cacheManager) {
        return new SessionCache(cacheManager, SESSION_CACHE_NAME);
    }

    @Bean
    public UserCache userCache(CacheManager cacheManager) {
        return ScmUserCacheFactory.create(cacheManager, USER_CACHE_NAME);
    }
}
