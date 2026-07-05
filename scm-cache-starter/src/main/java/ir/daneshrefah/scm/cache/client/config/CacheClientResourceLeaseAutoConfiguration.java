package ir.daneshrefah.scm.cache.client.config;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.HazelcastResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.LocalResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.RoutingResourceLeaseUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@AutoConfiguration(after = {CacheClientAutoConfiguration.class, CacheClientEventAutoConfiguration.class})
@EnableConfigurationProperties(CacheClientProperties.class)
@Slf4j
public class CacheClientResourceLeaseAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ResourceLeaseUtility resourceLeaseUtility(Optional<HazelcastInstance> hazelcastInstance,
                                                     CacheClientProperties cacheProperties,
                                                     ScmCacheEventSupport cacheEventSupport) {
        ResourceLeaseUtility remoteResourceLeaseUtility = null;
        if (cacheProperties.getUtilities().requiresRemoteResourceLease()) {
            remoteResourceLeaseUtility = new HazelcastResourceLeaseUtility(
                    requireHazelcast(hazelcastInstance, "resource-lease")
            );
        }
        log.info("ResourceLeaseUtility uses per-name backend routing");
        return new RoutingResourceLeaseUtility(
                new LocalResourceLeaseUtility(),
                remoteResourceLeaseUtility,
                cacheProperties.getUtilities(),
                cacheEventSupport
        );
    }

    private HazelcastInstance requireHazelcast(Optional<HazelcastInstance> hazelcastInstance, String utilityName) {
        return hazelcastInstance.orElseThrow(() -> new IllegalStateException(
                "HazelcastInstance is required for REMOTE " + utilityName + " utility backend"));
    }
}
