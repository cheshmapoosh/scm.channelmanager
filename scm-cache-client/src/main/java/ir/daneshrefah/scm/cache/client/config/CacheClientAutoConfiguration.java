package ir.daneshrefah.scm.cache.client.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.config.exception.HazelCastClientInitializationException;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.client.config.properties.CacheType;
import ir.daneshrefah.scm.cache.client.config.properties.RateLimitProperties;
import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.cache.client.connector.CacheTemplateImpl;
import ir.daneshrefah.scm.cache.client.connector.QueueTemplate;
import ir.daneshrefah.scm.cache.client.connector.QueueTemplateImpl;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.client.connector.backend.HazelcastCacheBackend;
import ir.daneshrefah.scm.cache.client.connector.backend.LocalCaffeineCacheBackend;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRouteResolver;
import ir.daneshrefah.scm.cache.client.connector.spring.RoutingCacheManager;
import ir.daneshrefah.scm.cache.client.utility.lock.HazelcastLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.aspect.WithLockAspect;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.Bucket4jRateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.aspect.RateLimiterAspect;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.HazelcastRateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.RateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.semaphore.HazelcastSemaphoreUtility;
import ir.daneshrefah.scm.cache.client.utility.semaphore.SemaphoreUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@AutoConfiguration
@EnableCaching
@Slf4j
@EnableConfigurationProperties({CacheClientProperties.class, RateLimitProperties.class})
public class CacheClientAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "scm.cache.client.config", ignoreUnknownFields = false)
    public ClientConfig clientConfig() {
        return new ClientConfig();
    }

    @Bean
    @ConditionalOnMissingBean(HazelcastInstance.class)
    @ConditionalOnProperty(prefix = "scm.cache.client", name = "distributed", havingValue = "true", matchIfMissing = true)
    public HazelcastInstance hazelcastClient(ClientConfig clientConfig, CacheClientProperties cacheProperties) {
        try {
            configureNearCaches(clientConfig, cacheProperties);
            log.info("Starting distributed Hazelcast client");
            return HazelcastClient.newHazelcastClient(clientConfig);
        } catch (Exception exception) {
            log.error("Could not initialize hazelcast client", exception);
            throw new HazelCastClientInitializationException(exception);
        }
    }

    @Bean
    @ConditionalOnMissingBean(HazelcastInstance.class)
    @ConditionalOnProperty(prefix = "scm.cache.client", name = "distributed", havingValue = "false")
    public HazelcastInstance hazelcastEmbed() {
        log.info("Starting embedded Hazelcast instance because scm.cache.client.distributed=false");
        return Hazelcast.newHazelcastInstance();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheRouteResolver cacheRouteResolver(CacheClientProperties cacheProperties) {
        return new CacheRouteResolver(cacheProperties);
    }

    @Bean
    @ConditionalOnMissingBean(name = "localCaffeineCacheBackend")
    public CacheBackend localCaffeineCacheBackend() {
        return new LocalCaffeineCacheBackend();
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean(name = "remoteHazelcastCacheBackend")
    public CacheBackend remoteHazelcastCacheBackend(HazelcastInstance hazelcastInstance) {
        return new HazelcastCacheBackend(hazelcastInstance, CacheType.REMOTE);
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean(name = "nearHazelcastCacheBackend")
    public CacheBackend nearHazelcastCacheBackend(HazelcastInstance hazelcastInstance) {
        return new HazelcastCacheBackend(hazelcastInstance, CacheType.NEAR);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheBackendRouter cacheBackendRouter(List<CacheBackend> backends) {
        return new CacheBackendRouter(backends);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager(CacheRouteResolver routeResolver,
                                     CacheBackendRouter backendRouter,
                                     CacheClientProperties cacheProperties) {
        return new RoutingCacheManager(routeResolver, backendRouter, cacheProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheTemplate cacheTemplate(CacheRouteResolver routeResolver,
                                       CacheBackendRouter backendRouter,
                                       Optional<HazelcastInstance> hazelcastInstance) {
        return new CacheTemplateImpl(routeResolver, backendRouter, hazelcastInstance);
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean
    public QueueTemplate queueTemplate(HazelcastInstance hazelcastInstance) {
        return new QueueTemplateImpl(hazelcastInstance);
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean
    public RateLimitBucketService rateLimitBucketService(
            HazelcastInstance hazelcastInstance,
            RateLimitProperties rateLimitProperties
    ) {
        return new HazelcastRateLimitBucketService(hazelcastInstance, rateLimitProperties);
    }

    @Bean
    @ConditionalOnBean(RateLimitBucketService.class)
    @ConditionalOnMissingBean
    public RateLimiterUtility rateLimiterUtility(RateLimitBucketService rateLimitBucketService,
                                                 RateLimitProperties rateLimitProperties) {
        return new Bucket4jRateLimiterUtility(rateLimitBucketService, rateLimitProperties);
    }

    @Bean
    @ConditionalOnBean(RateLimiterUtility.class)
    @ConditionalOnMissingBean
    public RateLimiterAspect rateLimiterAspect(RateLimiterUtility rateLimiterUtility) {
        return new RateLimiterAspect(rateLimiterUtility);
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean
    public LockUtility lockUtility(HazelcastInstance hazelcastInstance) {
        return new HazelcastLockUtility(hazelcastInstance);
    }

    @Bean
    @ConditionalOnBean(LockUtility.class)
    @ConditionalOnMissingBean
    public WithLockAspect withLockAspect(LockUtility lockUtility) {
        return new WithLockAspect(lockUtility);
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean
    public SemaphoreUtility semaphoreUtility(HazelcastInstance hazelcastInstance) {
        return new HazelcastSemaphoreUtility(hazelcastInstance);
    }

    private void configureNearCaches(ClientConfig clientConfig, CacheClientProperties cacheProperties) {
        for (Map.Entry<String, CacheClientProperties.CacheDefinition> entry : cacheProperties.getCaches().entrySet()) {
            String cacheName = entry.getKey();
            CacheClientProperties.CacheDefinition definition = entry.getValue();
            if (definition == null || definition.getType() != CacheType.NEAR) {
                continue;
            }
            String targetName = StringUtils.hasText(definition.getRemoteName()) ? definition.getRemoteName() : cacheName;
            NearCacheConfig nearCacheConfig = resolveOrCreateNearCache(clientConfig, targetName, cacheProperties.getDefaultMaximumSize());
            clientConfig.addNearCacheConfig(nearCacheConfig);
            log.info("Near cache enabled for remote cache '{}' (target='{}')", cacheName, targetName);
        }
    }

    private NearCacheConfig resolveOrCreateNearCache(ClientConfig clientConfig, String targetName, long defaultMaximumSize) {
        Map<String, NearCacheConfig> nearCacheConfigMap = clientConfig.getNearCacheConfigMap();
        NearCacheConfig existing = nearCacheConfigMap == null ? null : nearCacheConfigMap.get(targetName);
        if (existing != null) {
            return existing;
        }

        EvictionConfig evictionConfig = new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                .setSize(defaultMaximumSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) defaultMaximumSize);

        return new NearCacheConfig(targetName)
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setInvalidateOnChange(true)
                .setCacheLocalEntries(false)
                .setEvictionConfig(evictionConfig);
    }
}
