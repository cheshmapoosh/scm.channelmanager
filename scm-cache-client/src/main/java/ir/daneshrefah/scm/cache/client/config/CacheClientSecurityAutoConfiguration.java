package ir.daneshrefah.scm.cache.client.config;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.security.UserDetailsKeyResolvingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.util.StringUtils;

@AutoConfiguration(after = {CacheClientAutoConfiguration.class, CacheAutoConfiguration.class})
@ConditionalOnClass(name = "org.springframework.security.core.userdetails.UserCache")
@EnableConfigurationProperties(CacheClientProperties.class)
@Slf4j
public class CacheClientSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(UserCache.class)
    @ConditionalOnProperty(
            prefix = "scm.cache.client.security.user-cache",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public UserCache springSecurityUserCache(CacheManager cacheManager, CacheClientProperties properties) {
        CacheClientProperties.UserCache userCacheProperties = properties.getSecurity().getUserCache();
        String cacheName = StringUtils.hasText(userCacheProperties.getCacheName())
                ? userCacheProperties.getCacheName()
                : "user_cache";
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + cacheName);
        }
        log.info("Spring Security UserCache mapped to cache '{}'", cacheName);
        return new SpringCacheBasedUserCache(new UserDetailsKeyResolvingCache(cache, userCacheProperties));
    }
}
