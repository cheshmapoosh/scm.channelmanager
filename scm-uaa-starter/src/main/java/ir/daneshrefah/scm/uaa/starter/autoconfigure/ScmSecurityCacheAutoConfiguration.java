package ir.daneshrefah.scm.uaa.starter.autoconfigure;

import ir.daneshrefah.scm.uaa.starter.properties.ScmSecurityCacheProperties;
import ir.daneshrefah.scm.uaa.common.core.ScmUserCacheFactory;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserCache;

@AutoConfiguration(afterName = {
        "ir.daneshrefah.scm.cache.client.config.CacheClientAutoConfiguration",
        "ir.daneshrefah.scm.cache.client.config.CacheClientSpringCacheAutoConfiguration",
        "org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration"
})
@ConditionalOnBean(CacheManager.class)
@EnableConfigurationProperties(ScmSecurityCacheProperties.class)
public class ScmSecurityCacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SessionCache.class)
    @ConditionalOnProperty(
            prefix = "scm.security.cache.session",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public SessionCache sessionCache(
            CacheManager cacheManager,
            ScmSecurityCacheProperties properties
    ) {
        return new SessionCache(cacheManager, properties.getSession().getCacheName());
    }

    @Bean
    @ConditionalOnMissingBean(UserCache.class)
    @ConditionalOnProperty(
            prefix = "scm.security.cache.user",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = false
    )
    public UserCache userCache(
            CacheManager cacheManager,
            ScmSecurityCacheProperties properties
    ) {
        return ScmUserCacheFactory.create(
                cacheManager,
                properties.getUser().getCacheName(),
                properties.getUser().getKeyExpression()
        );
    }
}
