package ir.daneshrefah.scm.cache.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.matcher.WildcardConfigPatternMatcher;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.domain.config.*;
import ir.daneshrefah.scm.cache.mapper.InstanceMapper;
import ir.daneshrefah.scm.cache.repository.InstanceCacheConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-19
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class HazelcastConfig {

    private final InstanceMapper instanceMapper;

    @Bean
    @ConfigurationProperties(prefix = "hazelcast.config", ignoreUnknownFields = false)
    Config config() {
        Config config = new Config();
        config.setConfigPatternMatcher(new WildcardConfigPatternMatcher());
        log.info(">>> hazelcast config loaded");
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(){
        return Hazelcast.newHazelcastInstance(config());
    }

    @Bean
    public Boolean setupInstanceConfig(InstanceCacheConfigRepository instanceCacheConfigRepository){
        initHazelcastElementsConfig(instanceCacheConfigRepository);
        return true;
    }

    private void initHazelcastElementsConfig(InstanceCacheConfigRepository instanceCacheConfigRepository) {
        List<InstanceConfigEntity> elements = instanceCacheConfigRepository.findAll();
        log.info(">>> fetched cache instance config from database");
        setupMapConfig(elements, config());
        log.info(">> map config loaded");
        setupMultiMapConfig(elements, config());
        log.info(">> replicated map config loaded");
        setupReplicatedMapConfig(elements, config());
        log.info(">> queue config loaded");
        setupQueueConfig(elements, config());
        log.info(">> topic config loaded");
        setupTopicConfig(elements, config());
        log.info(">> list config loaded");
        setupListConfig(elements, config());
        log.info(">> set config loaded");
        setupSetConfig(elements, config());
    }

    private <T> List<T> filterConfig(List<InstanceConfigEntity> elements, Class<T> target) {
        List<T> filteredResult = new ArrayList<>();
        elements
                .stream()
                .filter(instanceConfigEntity -> instanceConfigEntity.getClass() == target)
                .map(target::cast)
                .forEach(filteredResult::add);
        return filteredResult;
    }

    private void setupSetConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, SetCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToSetConfig)
                .forEach(setConfig -> {
                    config.addSetConfig(setConfig);
                    hazelcastInstance().getSet(setConfig.getName());
                    log.info("> {} SET created.",setConfig.getName());
                });
    }

    private void setupListConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, ListCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToListConfig)
                .forEach(listConfig -> {
                    config.addListConfig(listConfig);
                    hazelcastInstance().getList(listConfig.getName());
                    log.info("> {} LIST created.",listConfig.getName());
                });
    }

    private void setupTopicConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, TopicCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToTopicConfig)
                .forEach(topicConfig -> {
                    config.addTopicConfig(topicConfig);
                    hazelcastInstance().getTopic(topicConfig.getName());
                    log.info("> {} TOPIC created.",topicConfig.getName());
                });
    }

    private void setupQueueConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, QueueCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToQueueConfig)
                .forEach(queueConfig -> {
                    config.addQueueConfig(queueConfig);
                    hazelcastInstance().getQueue(queueConfig.getName());
                    log.info("> {} QUEUE created.",queueConfig.getName());
                });
    }

    private void setupReplicatedMapConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, ReplicatedMapCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToReplicatedConfig)
                .forEach(replicatedMapConfig -> {
                    config.addReplicatedMapConfig(replicatedMapConfig);
                    hazelcastInstance().getReplicatedMap(replicatedMapConfig.getName());
                    log.info("> {} REPLICATED MAP created.",replicatedMapConfig.getName());
                });
    }

    private void setupMultiMapConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, MultiMapCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToMultiMapConfig)
                .forEach(multiMapConfig -> {
                    config.addMultiMapConfig(multiMapConfig);
                    hazelcastInstance().getMultiMap(multiMapConfig.getName());
                    log.info("> {} MULTIMAP MAP created.",multiMapConfig.getName());
                });
    }

    private void setupMapConfig(List<InstanceConfigEntity> elements, Config config) {
        filterConfig(elements, MapCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToMapConfig)
                .forEach(mapConfig -> {
                    config.addMapConfig(mapConfig);
                    hazelcastInstance().getMap(mapConfig.getName());
                    log.info("> {} MAP created.",mapConfig.getName());
                });
    }

}
