package ir.daneshrefah.scm.provider.rest.autoconfigure;

import ir.daneshrefah.scm.cache.starter.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.provider.rest.camel.RestProviderComponent;
import ir.daneshrefah.scm.provider.rest.metrics.RestProviderMetrics;
import ir.daneshrefah.scm.provider.rest.ratelimit.CacheClientRestProviderRateLimiter;
import ir.daneshrefah.scm.provider.rest.ratelimit.NoopRestProviderRateLimiter;
import ir.daneshrefah.scm.provider.rest.ratelimit.RestProviderRateLimiter;
import ir.daneshrefah.scm.provider.rest.trace.RestObservationAttributeContributor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Slf4j
@ConditionalOnClass(CamelContext.class)
public class RestProviderAutoConfiguration {

    @Bean("scm-rest")
    @ConditionalOnMissingBean(name = "scm-rest")
    public RestProviderComponent restProviderComponent(ObjectProvider<CamelContext> camelContext) {
        RestProviderComponent component = new RestProviderComponent();
        CamelContext context = camelContext.getIfAvailable();
        if (context != null) {
            component.setCamelContext(context);
        }
        return component;
    }

    @Bean(name = "restProviderVirtualThreadExecutor", destroyMethod = "close")
    @ConditionalOnMissingBean(name = "restProviderVirtualThreadExecutor")
    public ExecutorService restProviderVirtualThreadExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }



    @Bean
    @ConditionalOnMissingBean
    public RestProviderMetrics restProviderMetrics() {
        return new RestProviderMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestObservationAttributeContributor restObservationAttributeContributor() {
        return new RestObservationAttributeContributor();
    }

    @Bean
    @ConditionalOnMissingBean
    public RestProviderRateLimiter restProviderRateLimiter(
            ObjectProvider<RateLimiterUtility> rateLimiterUtility,
            RestProviderMetrics metrics
    ) {
        RateLimiterUtility utility = rateLimiterUtility.getIfAvailable();
        if (utility == null) {
            log.warn("RateLimiterUtility not found; REST provider rate limiter falls back to noop. Runtime deployments should enable scm-cache-starter rate-limit.");
            return new NoopRestProviderRateLimiter();
        }
        return new CacheClientRestProviderRateLimiter(utility, metrics);
    }
}
