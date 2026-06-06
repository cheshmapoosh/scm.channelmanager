package ir.daneshrefah.scm.provider.shetab.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.camel.ShetabComponent;
import ir.daneshrefah.scm.provider.shetab.lease.CacheClientShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.lease.NoopShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.lease.ShetabEndpointLeaseManager;
import ir.daneshrefah.scm.provider.shetab.metrics.ShetabProviderMetrics;
import ir.daneshrefah.scm.provider.shetab.ratelimit.CacheClientShetabRateLimiter;
import ir.daneshrefah.scm.provider.shetab.ratelimit.NoopShetabRateLimiter;
import ir.daneshrefah.scm.provider.shetab.ratelimit.ShetabRateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
@ConditionalOnClass(CamelContext.class)
public class ShetabProviderAutoConfiguration {

    @Bean("shetab")
    @ConditionalOnMissingBean(name = "shetab")
    public ShetabComponent shetabComponent(ObjectProvider<CamelContext> camelContext) {
        ShetabComponent component = new ShetabComponent();
        CamelContext context = camelContext.getIfAvailable();
        if (context != null) {
            component.setCamelContext(context);
        }
        return component;
    }


    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public ShetabProviderMetrics shetabProviderMetrics() {
        return new ShetabProviderMetrics();
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
    public ShetabEndpointLeaseManager shetabEndpointLeaseManager(ObjectProvider<ResourceLeaseUtility> resourceLeaseUtility) {
        ResourceLeaseUtility utility = resourceLeaseUtility.getIfAvailable();
        if (utility == null) {
            log.warn("ResourceLeaseUtility not found; Shetab endpoint lease supports only single-endpoint providers. Multi-endpoint providers with endpoint-lease.enabled=true will fail until scm-cache-client resource-lease is enabled.");
            return new NoopShetabEndpointLeaseManager();
        }
        return new CacheClientShetabEndpointLeaseManager(utility);
    }
}
