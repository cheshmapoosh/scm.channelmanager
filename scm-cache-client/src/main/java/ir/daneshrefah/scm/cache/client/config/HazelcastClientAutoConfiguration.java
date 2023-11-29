package ir.daneshrefah.scm.cache.client.config;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.config.*;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.client.config.properties.HazelcastClientProperties;
import ir.daneshrefah.scm.cache.client.config.exception.HazelCastClientInitializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author dariush abdolahi
 * @version 1.0
 * @since 2023-11-22
 */
@EnableConfigurationProperties(HazelcastClientProperties.class)
@RequiredArgsConstructor
@Configuration
@EnableCaching
@Slf4j
public class HazelcastClientAutoConfiguration {

    private final HazelcastClientProperties clientProperties;

    @Bean
    public HazelcastInstance hazelcastClient() {
        try {
            log.info(">>> hazelcast client config loaded");
            ClientConfig clientConfig = new ClientConfig();
            clientConfig.setNetworkConfig(new ClientNetworkConfig().addAddress(getServerAddress()));
            clientConfig.setClusterName(clientProperties.getClusterName());
            setupNearCache(clientConfig);
            return HazelcastClient.newHazelcastClient(clientConfig);
        } catch (Exception exception) {
            log.error(">>> couldn't create client hazelcast instance", exception);
            throw new HazelCastClientInitializationException();
        }
    }

    private String getServerAddress() {
        return clientProperties.getServerHost() + ":" + clientProperties.getServerPort();
    }

    @Bean
    public CacheManager cacheManager() {
        return new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastClient());
    }

    private void setupNearCache(ClientConfig clientConfig) {
        if (clientProperties.isNearCacheEnabled()){
            EvictionConfig evictionConfig = setupEvictionConfig();
            NearCachePreloaderConfig preloaderConfig = setupNearCachePreloaderConfig();
            NearCacheConfig nearCacheConfig = new NearCacheConfig()
                    .setInMemoryFormat(InMemoryFormat.OBJECT)
                    .setSerializeKeys(false)
                    .setInvalidateOnChange(true)
                    .setTimeToLiveSeconds(10)
                    .setMaxIdleSeconds(10)
                    .setEvictionConfig(evictionConfig)
                    .setPreloaderConfig(preloaderConfig);
            clientConfig.getNearCacheConfigMap().put("near-cache-config", nearCacheConfig);
            log.info(">>> hazelcast client near cache config loaded");
        }
    }

    private EvictionConfig setupEvictionConfig() {
        return new EvictionConfig()
                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setSize(10000);

    }

    private NearCachePreloaderConfig setupNearCachePreloaderConfig() {
        return new NearCachePreloaderConfig().setEnabled(true);
    }

}
