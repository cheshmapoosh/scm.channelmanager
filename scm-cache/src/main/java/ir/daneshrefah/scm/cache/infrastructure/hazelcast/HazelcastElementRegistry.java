package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.ListConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.ReplicatedMapConfig;
import com.hazelcast.config.SetConfig;
import com.hazelcast.config.TopicConfig;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.ListCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.MapCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.MultiMapCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.QueueCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.ReplicatedMapCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.SetCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.TopicCacheConfigEntity;
import ir.daneshrefah.scm.cache.mapper.InstanceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class HazelcastElementRegistry {
    private final InstanceMapper instanceMapper;
    private final HazelcastElementDefinitionFactory definitionFactory;
    private final HazelcastElementMaterializer materializer;

    public HazelcastInitializationPlan register(
            Config config,
            List<InstanceConfigEntity> definitions
    ) {
        List<HazelcastElementDefinition> elements = new ArrayList<>();
        for (InstanceConfigEntity definition : definitions) {
            elements.add(register(config, definition));
        }
        return new HazelcastInitializationPlan(elements);
    }

    public void materialize(
            HazelcastInstance instance,
            HazelcastInitializationPlan plan
    ) {
        materializer.materialize(instance, plan);
    }

    private HazelcastElementDefinition register(
            Config config,
            InstanceConfigEntity definition
    ) {
        if (definition instanceof MapCacheConfigEntity mapDefinition) {
            MapConfig mapConfig = instanceMapper.mapToMapConfig(mapDefinition);
            String name = normalizeName(mapConfig.getName(), HazelcastElementType.MAP);
            mapConfig.setName(name);
            config.addMapConfig(mapConfig);
            return definitionFactory.map(name, config.getMapConfig(name));
        }
        if (definition instanceof MultiMapCacheConfigEntity multiMapDefinition) {
            MultiMapConfig multiMapConfig = instanceMapper.mapToMultiMapConfig(multiMapDefinition);
            String name = normalizeName(multiMapConfig.getName(), HazelcastElementType.MULTI_MAP);
            multiMapConfig.setName(name);
            config.addMultiMapConfig(multiMapConfig);
            return definitionFactory.multiMap(name, config.getMultiMapConfig(name));
        }
        if (definition instanceof ReplicatedMapCacheConfigEntity replicatedMapDefinition) {
            ReplicatedMapConfig replicatedMapConfig = instanceMapper.mapToReplicatedConfig(replicatedMapDefinition);
            String name = normalizeName(replicatedMapConfig.getName(), HazelcastElementType.REPLICATED_MAP);
            replicatedMapConfig.setName(name);
            config.addReplicatedMapConfig(replicatedMapConfig);
            return definitionFactory.replicatedMap(name, config.getReplicatedMapConfig(name));
        }
        if (definition instanceof QueueCacheConfigEntity queueDefinition) {
            QueueConfig queueConfig = instanceMapper.mapToQueueConfig(queueDefinition);
            String name = normalizeName(queueConfig.getName(), HazelcastElementType.QUEUE);
            queueConfig.setName(name);
            config.addQueueConfig(queueConfig);
            return definitionFactory.queue(name, config.getQueueConfig(name));
        }
        if (definition instanceof TopicCacheConfigEntity topicDefinition) {
            TopicConfig topicConfig = instanceMapper.mapToTopicConfig(topicDefinition);
            String name = normalizeName(topicConfig.getName(), HazelcastElementType.TOPIC);
            topicConfig.setName(name);
            config.addTopicConfig(topicConfig);
            return definitionFactory.topic(name, config.getTopicConfig(name));
        }
        if (definition instanceof ListCacheConfigEntity listDefinition) {
            ListConfig listConfig = instanceMapper.mapToListConfig(listDefinition);
            String name = normalizeName(listConfig.getName(), HazelcastElementType.LIST);
            listConfig.setName(name);
            config.addListConfig(listConfig);
            return definitionFactory.list(name, config.getListConfig(name));
        }
        if (definition instanceof SetCacheConfigEntity setDefinition) {
            SetConfig setConfig = instanceMapper.mapToSetConfig(setDefinition);
            String name = normalizeName(setConfig.getName(), HazelcastElementType.SET);
            setConfig.setName(name);
            config.addSetConfig(setConfig);
            return definitionFactory.set(name, config.getSetConfig(name));
        }

        throw new IllegalArgumentException(
                "Unsupported Hazelcast cache definition type: " + definition.getClass().getName()
        );
    }

    private String normalizeName(String name, HazelcastElementType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hazelcast " + type + " configuration name must not be blank");
        }
        return name.trim();
    }
}
