package ir.daneshrefah.scm.cache.starter.config;

import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.starter.connector.routing.CacheRouteResolver;
import ir.daneshrefah.scm.cache.starter.connector.spring.RoutingCacheManager;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration(after = {CacheClientAutoConfiguration.class, CacheClientEventAutoConfiguration.class})
@EnableConfigurationProperties(CacheClientProperties.class)
public class CacheClientSpringCacheAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(CacheRouteResolver routeResolver,
                                     CacheBackendRouter backendRouter,
                                     CacheClientProperties cacheProperties,
                                     ScmCacheEventSupport cacheEventSupport) {
        return new RoutingCacheManager(routeResolver, backendRouter, cacheProperties, cacheEventSupport);
    }
}
