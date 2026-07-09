package ir.daneshrefah.scm.cache.starter.config;

import ir.daneshrefah.scm.cache.starter.event.CacheKeyHasher;
import ir.daneshrefah.scm.cache.starter.event.DefaultCacheKeyHasher;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CacheClientEventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheKeyHasher cacheKeyHasher() {
        return new DefaultCacheKeyHasher();
    }

    @Bean
    @ConditionalOnMissingBean
    public ScmCacheEventSupport scmCacheEventSupport(ObjectProvider<ScmEventPublisher> eventPublisherProvider,
                                                     CacheKeyHasher cacheKeyHasher) {
        return new ScmCacheEventSupport(eventPublisherProvider, cacheKeyHasher);
    }
}
