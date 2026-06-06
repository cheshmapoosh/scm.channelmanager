package ir.daneshrefah.scm.provider.shetab.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.common.provider.config.ProviderRegistryProperties;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactory;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerFactoryRegistry;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerPipelineFactory;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.provider.shetab.camel.ShetabComponent;
import ir.daneshrefah.scm.provider.shetab.config.ShetabProperties;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;

@Configuration
@Slf4j
@ConditionalOnClass(CamelContext.class)
@EnableConfigurationProperties({ShetabProperties.class, ProviderRegistryProperties.class})
@ConditionalOnProperty(prefix = "scm.provider.shetab", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    public ProviderMessageCustomizerFactoryRegistry providerMessageCustomizerFactoryRegistry(
            Collection<ProviderMessageCustomizerFactory<?>> factories
    ) {
        return new ProviderMessageCustomizerFactoryRegistry(factories);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProviderMessageCustomizerPipelineFactory providerMessageCustomizerPipelineFactory(
            ProviderMessageCustomizerFactoryRegistry registry,
            ObjectMapper objectMapper
    ) {
        return new ProviderMessageCustomizerPipelineFactory(registry, objectMapper);
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
            log.warn("ResourceLeaseUtility not found; Shetab endpoint lease falls back to first configured endpoint. Runtime deployments should enable scm-cache-client resource-lease.");
            return new NoopShetabEndpointLeaseManager();
        }
        return new CacheClientShetabEndpointLeaseManager(utility);
    }
}
