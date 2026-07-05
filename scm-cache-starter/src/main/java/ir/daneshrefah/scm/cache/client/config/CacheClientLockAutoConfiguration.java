package ir.daneshrefah.scm.cache.client.config;

import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.cache.client.utility.lock.HazelcastLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.LocalLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.RoutingLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.aspect.WithLockAspect;
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
public class CacheClientLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LockUtility lockUtility(Optional<HazelcastInstance> hazelcastInstance,
                                   CacheClientProperties cacheProperties,
                                   ScmCacheEventSupport cacheEventSupport) {
        LockUtility remoteLockUtility = null;
        if (cacheProperties.getUtilities().requiresRemoteLock()) {
            remoteLockUtility = new HazelcastLockUtility(requireHazelcast(hazelcastInstance, "lock"), cacheEventSupport);
        }
        log.info("LockUtility uses per-name backend routing");
        return new RoutingLockUtility(
                new LocalLockUtility(cacheEventSupport),
                remoteLockUtility,
                cacheProperties.getUtilities(),
                cacheEventSupport
        );
    }

    @Bean
    @ConditionalOnBean(LockUtility.class)
    @ConditionalOnMissingBean
    public WithLockAspect withLockAspect(LockUtility lockUtility) {
        return new WithLockAspect(lockUtility);
    }

    private HazelcastInstance requireHazelcast(Optional<HazelcastInstance> hazelcastInstance, String utilityName) {
        return hazelcastInstance.orElseThrow(() -> new IllegalStateException(
                "HazelcastInstance is required for REMOTE " + utilityName + " utility backend"));
    }
}
