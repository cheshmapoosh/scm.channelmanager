package ir.daneshrefah.scm.cache.starter.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.starter.config.exception.HazelCastClientInitializationException;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheClientProperties;
import ir.daneshrefah.scm.cache.starter.config.properties.CacheType;
import ir.daneshrefah.scm.cache.starter.connector.backend.CacheBackend;
import ir.daneshrefah.scm.cache.starter.connector.backend.CacheBackendRouter;
import ir.daneshrefah.scm.cache.starter.connector.backend.HazelcastCacheBackend;
import ir.daneshrefah.scm.cache.starter.connector.backend.LocalCaffeineCacheBackend;
import ir.daneshrefah.scm.cache.starter.connector.routing.CacheRouteResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@AutoConfiguration
@EnableCaching
@Slf4j
@EnableConfigurationProperties(CacheClientProperties.class)
public class CacheClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ClientConfig clientConfig(CacheClientProperties cacheProperties) {
        ClientConfig clientConfig = new ClientConfig();
        CacheClientProperties.RemoteProperties remote = cacheProperties.getRemote();
        if (remote != null) {
            if (StringUtils.hasText(remote.getClusterName())) {
                clientConfig.setClusterName(remote.getClusterName().trim());
            }
            if (remote.getAddresses() != null && !remote.getAddresses().isEmpty()) {
                clientConfig.getNetworkConfig().setAddresses(remote.getAddresses());
            }
        }
        configureNearCaches(clientConfig, cacheProperties);
        return clientConfig;
    }

    @Bean
    @ConditionalOnMissingBean(HazelcastInstance.class)
    @ConditionalOnProperty(prefix = "scm.cache.client", name = "distributed", havingValue = "true", matchIfMissing = true)
    public HazelcastInstance hazelcastClient(ClientConfig clientConfig, CacheClientProperties cacheProperties) {
        try {
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
    public HazelcastInstance hazelcastEmbed(CacheClientProperties cacheProperties) {
        validateNearCachesDisabled(cacheProperties);
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

    private void configureNearCaches(ClientConfig clientConfig, CacheClientProperties cacheProperties) {
        if (!hasNearCache(cacheProperties)) {
            return;
        }
        CacheClientProperties.NearProperties near = cacheProperties.getNear();
        if (near == null || !near.isEnabled()) {
            throw new IllegalStateException("Cache type NEAR requires scm.cache.client.near.enabled=true.");
        }
        for (Map.Entry<String, CacheClientProperties.CacheDefinition> entry : cacheProperties.getCaches().entrySet()) {
            String cacheName = entry.getKey();
            CacheClientProperties.CacheDefinition definition = entry.getValue();
            if (definition == null || definition.getType() != CacheType.NEAR) {
                continue;
            }
            String targetName = StringUtils.hasText(definition.getRemoteName()) ? definition.getRemoteName() : cacheName;
            long maximumSize = definition.getMaximumSize() != null && definition.getMaximumSize() > 0
                    ? definition.getMaximumSize()
                    : near.getMaximumSize();
            NearCacheConfig nearCacheConfig = resolveOrCreateNearCache(clientConfig, targetName, near, maximumSize);
            clientConfig.addNearCacheConfig(nearCacheConfig);
            log.info("Near cache enabled for remote cache '{}' (target='{}')", cacheName, targetName);
        }
    }

    private NearCacheConfig resolveOrCreateNearCache(
            ClientConfig clientConfig,
            String targetName,
            CacheClientProperties.NearProperties near,
            long maximumSize
    ) {
        Map<String, NearCacheConfig> nearCacheConfigMap = clientConfig.getNearCacheConfigMap();
        NearCacheConfig existing = nearCacheConfigMap == null ? null : nearCacheConfigMap.get(targetName);
        if (existing != null) {
            return existing;
        }

        EvictionConfig evictionConfig = new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                .setSize(maximumSize > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maximumSize);

        return new NearCacheConfig(targetName)
                .setInMemoryFormat(inMemoryFormat(near.getInMemoryFormat()))
                .setInvalidateOnChange(near.isInvalidateOnChange())
                .setCacheLocalEntries(false)
                .setEvictionConfig(evictionConfig);
    }

    private void validateNearCachesDisabled(CacheClientProperties cacheProperties) {
        if (hasNearCache(cacheProperties)) {
            throw new IllegalStateException("Cache type NEAR requires scm.cache.client.distributed=true and a remote Hazelcast client.");
        }
    }

    private boolean hasNearCache(CacheClientProperties cacheProperties) {
        return cacheProperties.getCaches().values().stream()
                .anyMatch(definition -> definition != null && definition.getType() == CacheType.NEAR);
    }

    private InMemoryFormat inMemoryFormat(String value) {
        String normalized = StringUtils.hasText(value) ? value.trim().toUpperCase(java.util.Locale.ROOT) : "OBJECT";
        return InMemoryFormat.valueOf(normalized);
    }
}
