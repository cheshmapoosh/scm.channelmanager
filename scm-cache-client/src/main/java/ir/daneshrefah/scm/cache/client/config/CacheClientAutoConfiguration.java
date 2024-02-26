package ir.daneshrefah.scm.cache.client.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.config.exception.HazelCastClientInitializationException;
import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdolahi
 * @version 1.0
 * @since 2023-11-22
 */
@EnableConfigurationProperties({CacheClientProperties.class})
@RequiredArgsConstructor
@Configuration
@EnableCaching
@Slf4j
public class CacheClientAutoConfiguration {

    private final CacheClientProperties clientProperties;

    @Bean
    @ConditionalOnProperty(name = "scm.cache.client.config.distributed", havingValue = "true", matchIfMissing = true)
    public HazelcastInstance hazelcastClient() {
        try {
            log.info(">>> hazelcast client config loaded");
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setNetworkConfig(new ClientNetworkConfig().addAddress(getServerAddress()));
            clientConfig.setClusterName(clientProperties.getClusterName());
            setupNearCache(clientConfig);
            setupSerializationConfig(clientConfig);
            return HazelcastClient.newHazelcastClient(clientConfig);
        } catch (Exception exception) {
            log.error(">>> couldn't create client hazelcast instance", exception);
            throw new HazelCastClientInitializationException();
        }
    }

    @Bean
    @ConditionalOnProperty(name = "scm.cache.client.config.distributed", havingValue = "false", matchIfMissing = false)
    public HazelcastInstance hazelcastEmbed() {
        log.info(">>> hazelcast local iit started");
        return Hazelcast.newHazelcastInstance();
    }

    private String getServerAddress() {
        return clientProperties.getServerHost() + ":" + clientProperties.getServerPort();
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
        return new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastInstance);
    }

    private void setupNearCache(ClientConfig clientConfig) {
        if (Objects.nonNull(clientProperties.getNearCacheConfig())) {
            Map<String, NearCacheConfig> nearCacheConfigMap = clientProperties.getNearCacheConfig();
            nearCacheConfigMap
                    .keySet()
                    .forEach(cacheName -> {
                        NearCacheConfig nearCacheConfig = nearCacheConfigMap.get(cacheName);
                        nearCacheConfig.setName(cacheName);
                        clientConfig.addNearCacheConfig(nearCacheConfig);
                        log.info(">>> hazelcast client near cache config has been loaded for {}", cacheName);
                    });
        }
    }

    private void setupSerializationConfig(ClientConfig clientConfig){
        if (Objects.nonNull(clientProperties.getSerializationConfig())){
            SerializationConfig serializationConfig = clientProperties.getSerializationConfig();
            clientConfig.setSerializationConfig(serializationConfig);
            log.info(">>> hazelcast client serialization config has been loaded");
        }
    }

}
