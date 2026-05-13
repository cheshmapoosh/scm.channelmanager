package ir.daneshrefah.scm.provider.shetab.autoconfigure;

import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.camel.ShetabComponent;
import ir.daneshrefah.scm.provider.shetab.config.ShetabProperties;
import ir.daneshrefah.scm.provider.shetab.lease.CacheClientShetabPortLeaseManager;
import ir.daneshrefah.scm.provider.shetab.lease.NoopShetabPortLeaseManager;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabPortLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.ratelimit.CacheClientShetabRateLimiter;
import ir.daneshrefah.scm.provider.shetab.ratelimit.NoopShetabRateLimiter;
import ir.daneshrefah.scm.provider.shetab.ratelimit.ShetabRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@ConditionalOnClass(CamelContext.class)
@EnableConfigurationProperties(ShetabProperties.class)
@ConditionalOnProperty(prefix = "scm.provider.shetab", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ShetabProviderAutoConfiguration {

    @Bean("shetab")
    @ConditionalOnMissingBean(name = "shetab")
    public ShetabComponent shetabComponent(CamelContext camelContext) {
        ShetabComponent component = new ShetabComponent();
        component.setCamelContext(camelContext);
        return component;
    }

    @Bean
    @ConditionalOnMissingBean
    public ShetabRateLimiter shetabRateLimiter(ObjectProvider<RateLimiterUtility> rateLimiterUtility, ShetabProviderMetrics metrics) {
        RateLimiterUtility utility = rateLimiterUtility.getIfAvailable();
        if (utility == null) {
            log.warn("RateLimiterUtility not found; Shetab rate limiter falls back to noop. Runtime deployments should enable scm-cache-client rate-limit.");
            return new NoopShetabRateLimiter();
        }
        return new CacheClientShetabRateLimiter(utility, metrics);
    }

    @Bean
    @ConditionalOnMissingBean
    public ShetabPortLeaseManager shetabPortLeaseManager(ObjectProvider<ResourceLeaseUtility> resourceLeaseUtility) {
        ResourceLeaseUtility utility = resourceLeaseUtility.getIfAvailable();
        if (utility == null) {
            log.warn("ResourceLeaseUtility not found; Shetab local port lease falls back to noop. Runtime deployments should enable scm-cache-client resource-lease.");
            return new NoopShetabPortLeaseManager();
        }
        return new CacheClientShetabPortLeaseManager(utility);
    }
}
