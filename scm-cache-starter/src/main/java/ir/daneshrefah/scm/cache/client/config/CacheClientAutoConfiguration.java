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
import ir.daneshrefah.scm.cache.client.connector.QueueTemplate;
import ir.daneshrefah.scm.cache.client.connector.QueueTemplateImpl;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.client.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.client.connector.backend.HazelcastCacheBackend;
import ir.daneshrefah.scm.cache.client.connector.backend.LocalCaffeineCacheBackend;
import ir.daneshrefah.scm.cache.client.connector.routing.CacheRouteResolver;
import ir.daneshrefah.scm.cache.client.connector.spring.RoutingCacheManager;
import ir.daneshrefah.scm.cache.client.event.ScmCacheEventSupport;
import ir.daneshrefah.scm.cache.client.utility.lock.HazelcastLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.LocalLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.LockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.RoutingLockUtility;
import ir.daneshrefah.scm.cache.client.utility.lock.aspect.WithLockAspect;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.Bucket4jRateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.RateLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.aspect.RateLimiterAspect;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.HazelcastRateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.LocalRateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.RateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.ratelimit.backend.RoutingRateLimitBucketService;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.HazelcastResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.LocalResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.RoutingResourceLeaseUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.HazelcastConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.LocalConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.ConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.RoutingConcurrencyLimiterUtility;
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.aspect.WithConcurrencyLimitAspect;
import ir.daneshrefah.scm.common.event.ScmEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
    public ScmCacheEventSupport scmCacheEventSupport(ObjectProvider<ScmEventPublisher> eventPublisherProvider) {
        return new ScmCacheEventSupport(eventPublisherProvider);
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
                                     CacheClientProperties cacheProperties,
                                     ScmCacheEventSupport cacheEventSupport) {
        return new RoutingCacheManager(routeResolver, backendRouter, cacheProperties, cacheEventSupport);
    }

    @Bean
    @ConditionalOnBean(HazelcastInstance.class)
    @ConditionalOnMissingBean
    public QueueTemplate queueTemplate(HazelcastInstance hazelcastInstance) {
        return new QueueTemplateImpl(hazelcastInstance);
    }

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

    @Bean
    @ConditionalOnMissingBean
    public LockUtility lockUtility(Optional<HazelcastInstance> hazelcastInstance,
                                   CacheClientProperties cacheProperties,
                                   ScmCacheEventSupport cacheEventSupport) {
        LockUtility remoteLockUtility = null;
        if (cacheProperties.getUtilities().requiresRemoteLock()) {
            remoteLockUtility = new HazelcastLockUtility(requireHazelcast(hazelcastInstance, "lock"));
        }
        log.info("LockUtility uses per-name backend routing");
        return new RoutingLockUtility(new LocalLockUtility(), remoteLockUtility, cacheProperties.getUtilities(), cacheEventSupport);
    }

    @Bean
    @ConditionalOnBean(LockUtility.class)
    @ConditionalOnMissingBean
    public WithLockAspect withLockAspect(LockUtility lockUtility) {
        return new WithLockAspect(lockUtility);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConcurrencyLimiterUtility concurrencyLimiterUtility(Optional<HazelcastInstance> hazelcastInstance,
                                                               CacheClientProperties cacheProperties) {
        ConcurrencyLimiterUtility remoteConcurrencyLimiterUtility = null;
        if (cacheProperties.getUtilities().requiresRemoteConcurrencyLimit()) {
            remoteConcurrencyLimiterUtility = new HazelcastConcurrencyLimiterUtility(
                    requireHazelcast(hazelcastInstance, "concurrency-limit")
            );
        }
        log.info("ConcurrencyLimiterUtility uses per-name backend routing");
        return new RoutingConcurrencyLimiterUtility(
                new LocalConcurrencyLimiterUtility(),
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

    @Bean
    @ConditionalOnMissingBean
    public ResourceLeaseUtility resourceLeaseUtility(Optional<HazelcastInstance> hazelcastInstance,
                                                     CacheClientProperties cacheProperties) {
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
                cacheProperties.getUtilities()
        );
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

    private HazelcastInstance requireHazelcast(Optional<HazelcastInstance> hazelcastInstance, String utilityName) {
        return hazelcastInstance.orElseThrow(() -> new IllegalStateException(
                "HazelcastInstance is required for REMOTE " + utilityName + " utility backend"));
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
