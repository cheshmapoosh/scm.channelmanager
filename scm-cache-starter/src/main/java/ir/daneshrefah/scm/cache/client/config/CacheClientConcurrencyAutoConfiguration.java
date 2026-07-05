package ir.daneshrefah.scm.cache.client.config;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.ConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.HazelcastConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.LocalConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.RoutingConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.aspect.WithConcurrencyLimitAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@AutoConfiguration(after = {CacheClientAutoConfiguration.class, CacheClientEventAutoConfiguration.class})
@EnableConfigurationProperties(CacheClientProperties.class)
@Slf4j
public class CacheClientConcurrencyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConcurrencyLimiterUtility concurrencyLimiterUtility(Optional<HazelcastInstance> hazelcastInstance,
                                                               CacheClientProperties cacheProperties,
                                                               ScmCacheEventSupport cacheEventSupport) {
        ConcurrencyLimiterUtility remoteConcurrencyLimiterUtility = null;
        if (cacheProperties.getUtilities().requiresRemoteConcurrencyLimit()) {
            remoteConcurrencyLimiterUtility = new HazelcastConcurrencyLimiterUtility(
                    requireHazelcast(hazelcastInstance, "concurrency-limit"),
                    cacheEventSupport
            );
        }
        log.info("ConcurrencyLimiterUtility uses per-name backend routing");
        return new RoutingConcurrencyLimiterUtility(
                new LocalConcurrencyLimiterUtility(cacheEventSupport),
                remoteConcurrencyLimiterUtility,
                cacheProperties.getUtilities()
        );
    }

    @Bean
    @ConditionalOnBean(ConcurrencyLimiterUtility.class)
    @ConditionalOnMissingBean
    public WithConcurrencyLimitAspect withConcurrencyLimitAspect(ConcurrencyLimiterUtility concurrencyLimiterUtility) {
        return new WithConcurrencyLimitAspect(concurrencyLimiterUtility);
    }

    private HazelcastInstance requireHazelcast(Optional<HazelcastInstance> hazelcastInstance, String utilityName) {
        return hazelcastInstance.orElseThrow(() -> new IllegalStateException(
                "HazelcastInstance is required for REMOTE " + utilityName + " utility backend"));
    }
}
