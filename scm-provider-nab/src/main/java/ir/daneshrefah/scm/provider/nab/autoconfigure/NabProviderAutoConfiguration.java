package ir.daneshrefah.scm.provider.nab.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.provider.nab.camel.NabComponent;
import ir.daneshrefah.scm.provider.nab.metrics.NabProviderMetrics;
import ir.daneshrefah.scm.provider.nab.ratelimit.CacheClientNabRateLimiter;
import ir.daneshrefah.scm.provider.nab.ratelimit.NabRateLimiter;
import ir.daneshrefah.scm.provider.nab.ratelimit.NoopNabRateLimiter;
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
public class NabProviderAutoConfiguration {

    @Bean("scm-nab")
    @ConditionalOnMissingBean(name = "scm-nab")
    public NabComponent nabComponent(ObjectProvider<CamelContext> camelContext) {
        NabComponent component = new NabComponent();
        CamelContext context = camelContext.getIfAvailable();
        if (context != null) {
            component.setCamelContext(context);
        }
        return component;
    }

    @Bean
    @ConditionalOnMissingBean
    public NabProviderMetrics nabProviderMetrics() {
        return new NabProviderMetrics();
    }

    @Bean
    @ConditionalOnMissingBean
    public NabRateLimiter nabRateLimiter(ObjectProvider<RateLimiterUtility> rateLimiterUtility, NabProviderMetrics metrics) {
        RateLimiterUtility utility = rateLimiterUtility.getIfAvailable();
        if (utility == null) {
            log.warn("RateLimiterUtility not found; NAB rate limiter falls back to noop. Runtime deployments should enable scm-cache-client rate-limit.");
            return new NoopNabRateLimiter();
        }
        return new CacheClientNabRateLimiter(utility, metrics);
    }
}
