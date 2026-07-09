package ir.daneshrefah.scm.cache.starter.config;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.config.properties.RateLimitProperties;
import ir.daneshrefah.scm.cache.starter.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.Bucket4jRateLimiterUtility;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.aspect.RateLimiterAspect;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.backend.HazelcastRateLimitBucketService;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.backend.LocalRateLimitBucketService;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.backend.RateLimitBucketService;
import ir.daneshrefah.scm.cache.starter.utility.ratelimit.backend.RoutingRateLimitBucketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@AutoConfiguration(after = {CacheClientAutoConfiguration.class, CacheClientEventAutoConfiguration.class})
@EnableConfigurationProperties({CacheClientProperties.class, RateLimitProperties.class})
@Slf4j
public class CacheClientRateLimitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RateLimitBucketService rateLimitBucketService(Optional<HazelcastInstance> hazelcastInstance,
                                                         RateLimitProperties rateLimitProperties,
                                                         CacheClientProperties cacheProperties) {
        LocalRateLimitBucketService localBucketService = new LocalRateLimitBucketService(rateLimitProperties);
        localBucketService.configure();

        HazelcastRateLimitBucketService remoteBucketService = null;
        if (cacheProperties.getUtilities().requiresRemoteRateLimit()) {
            remoteBucketService = new HazelcastRateLimitBucketService(
                    requireHazelcast(hazelcastInstance, "rate-limit"),
                    rateLimitProperties
            );
            remoteBucketService.configure();
        }
        log.info("RateLimiterUtility uses per-name backend routing");
        return new RoutingRateLimitBucketService(localBucketService, remoteBucketService, cacheProperties.getUtilities());
    }

    @Bean
    @ConditionalOnBean(RateLimitBucketService.class)
    @ConditionalOnMissingBean
    public RateLimiterUtility rateLimiterUtility(RateLimitBucketService rateLimitBucketService,
                                                 RateLimitProperties rateLimitProperties,
                                                 ScmCacheEventSupport cacheEventSupport) {
        return new Bucket4jRateLimiterUtility(rateLimitBucketService, rateLimitProperties, cacheEventSupport);
    }

    @Bean
    @ConditionalOnBean(RateLimiterUtility.class)
    @ConditionalOnMissingBean
    public RateLimiterAspect rateLimiterAspect(RateLimiterUtility rateLimiterUtility) {
        return new RateLimiterAspect(rateLimiterUtility);
    }

    private HazelcastInstance requireHazelcast(Optional<HazelcastInstance> hazelcastInstance, String utilityName) {
        return hazelcastInstance.orElseThrow(() -> new IllegalStateException(
                "HazelcastInstance is required for REMOTE " + utilityName + " utility backend"));
    }
}
