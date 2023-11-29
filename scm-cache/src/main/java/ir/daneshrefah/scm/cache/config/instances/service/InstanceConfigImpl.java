package ir.daneshrefah.scm.cache.config.instances.service;

import com.hazelcast.config.Config;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.config.instances.domain.*;
import ir.daneshrefah.scm.cache.config.instances.mapper.InstanceMapper;
import ir.daneshrefah.scm.cache.config.instances.repository.InstanceCacheConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class InstanceConfigImpl implements InstanceConfig{
    private static final List<InstanceConfigEntity> CACHE_CONFIG_ENTITIES = new ArrayList<>();
    private final InstanceMapper instanceMapper;
    private final InstanceCacheConfigRepository instanceCacheConfigRepository;
    private final HazelcastInstance hazelcastInstance;

    private void fetchAll() {
        CACHE_CONFIG_ENTITIES.addAll(instanceCacheConfigRepository.findAll());
    }


    private <T> List<T> filterConfig(Class<T> target) {
        List<T> filteredResult = new ArrayList<>();
        CACHE_CONFIG_ENTITIES
                .stream()
                .filter(instanceConfigEntity -> instanceConfigEntity.getClass() == target)
                .map(target::cast)
                .forEach(filteredResult::add);
        return filteredResult;
    }


    @Override
    public void setup(Config config) {
        fetchAll();
        log.info(">>> fetched cache instance config from database");
        setupMapConfig(config);
        log.info(">> map config loaded");
        setupMultiMapConfig(config);
        log.info(">> replicated map config loaded");
        setupReplicatedMapConfig(config);
        log.info(">> queue config loaded");
        setupQueueConfig(config);
        log.info(">> topic config loaded");
        setupTopicConfig(config);
        log.info(">> list config loaded");
        setupListConfig(config);
        log.info(">> set config loaded");
        setupSetConfig(config);
    }

    private void setupSetConfig(Config config) {
        filterConfig(SetCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToSetConfig)
                .forEach(setConfig -> {
                    config.addSetConfig(setConfig);
                    hazelcastInstance.getSet(setConfig.getName());
                    log.info("> {} SET created.",setConfig.getName());
                });
    }

    private void setupListConfig(Config config) {
        filterConfig(ListCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToListConfig)
                .forEach(listConfig -> {
                    config.addListConfig(listConfig);
                    hazelcastInstance.getList(listConfig.getName());
                    log.info("> {} LIST created.",listConfig.getName());
                });
    }

    private void setupTopicConfig(Config config) {
        filterConfig(TopicCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToTopicConfig)
                .forEach(topicConfig -> {
                    config.addTopicConfig(topicConfig);
                    hazelcastInstance.getTopic(topicConfig.getName());
                    log.info("> {} TOPIC created.",topicConfig.getName());
                });
    }

    private void setupQueueConfig(Config config) {
        filterConfig(QueueCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToQueueConfig)
                .forEach(queueConfig -> {
                    config.addQueueConfig(queueConfig);
                    hazelcastInstance.getQueue(queueConfig.getName());
                    log.info("> {} QUEUE created.",queueConfig.getName());
                });
    }

    private void setupReplicatedMapConfig(Config config) {
        filterConfig(ReplicatedMapCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToReplicatedConfig)
                .forEach(replicatedMapConfig -> {
                    config.addReplicatedMapConfig(replicatedMapConfig);
                    hazelcastInstance.getReplicatedMap(replicatedMapConfig.getName());
                    log.info("> {} REPLICATED MAP created.",replicatedMapConfig.getName());
                });
    }

    private void setupMultiMapConfig(Config config) {
        filterConfig(MultiMapCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToMultiMapConfig)
                .forEach(multiMapConfig -> {
                    config.addMultiMapConfig(multiMapConfig);
                    hazelcastInstance.getMultiMap(multiMapConfig.getName());
                    log.info("> {} MULTIMAP MAP created.",multiMapConfig.getName());
                });
    }

    private void setupMapConfig(Config config) {
        filterConfig(MapCacheConfigEntity.class)
                .stream()
                .map(instanceMapper::mapToMapConfig)
                .forEach(mapConfig -> {
                    config.addMapConfig(mapConfig);
                    hazelcastInstance.getMap(mapConfig.getName());
                    log.info("> {} MAP created.",mapConfig.getName());
                });
    }
}
