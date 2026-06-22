package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
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
        for (HazelcastElementDefinition element : plan.elements()) {
            materialize(instance, element);
        }
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
            return mapElement(name, config.getMapConfig(name));
        }
        if (definition instanceof MultiMapCacheConfigEntity multiMapDefinition) {
            MultiMapConfig multiMapConfig = instanceMapper.mapToMultiMapConfig(multiMapDefinition);
            String name = normalizeName(multiMapConfig.getName(), HazelcastElementType.MULTI_MAP);
            multiMapConfig.setName(name);
            config.addMultiMapConfig(multiMapConfig);
            return multiMapElement(name, config.getMultiMapConfig(name));
        }
        if (definition instanceof ReplicatedMapCacheConfigEntity replicatedMapDefinition) {
            ReplicatedMapConfig replicatedMapConfig = instanceMapper.mapToReplicatedConfig(replicatedMapDefinition);
            String name = normalizeName(replicatedMapConfig.getName(), HazelcastElementType.REPLICATED_MAP);
            replicatedMapConfig.setName(name);
            config.addReplicatedMapConfig(replicatedMapConfig);
            return replicatedMapElement(name, config.getReplicatedMapConfig(name));
        }
        if (definition instanceof QueueCacheConfigEntity queueDefinition) {
            QueueConfig queueConfig = instanceMapper.mapToQueueConfig(queueDefinition);
            String name = normalizeName(queueConfig.getName(), HazelcastElementType.QUEUE);
            queueConfig.setName(name);
            config.addQueueConfig(queueConfig);
            return queueElement(name, config.getQueueConfig(name));
        }
        if (definition instanceof TopicCacheConfigEntity topicDefinition) {
            TopicConfig topicConfig = instanceMapper.mapToTopicConfig(topicDefinition);
            String name = normalizeName(topicConfig.getName(), HazelcastElementType.TOPIC);
            topicConfig.setName(name);
            config.addTopicConfig(topicConfig);
            return topicElement(name, config.getTopicConfig(name));
        }
        if (definition instanceof ListCacheConfigEntity listDefinition) {
            ListConfig listConfig = instanceMapper.mapToListConfig(listDefinition);
            String name = normalizeName(listConfig.getName(), HazelcastElementType.LIST);
            listConfig.setName(name);
            config.addListConfig(listConfig);
            return listElement(name, config.getListConfig(name));
        }
        if (definition instanceof SetCacheConfigEntity setDefinition) {
            SetConfig setConfig = instanceMapper.mapToSetConfig(setDefinition);
            String name = normalizeName(setConfig.getName(), HazelcastElementType.SET);
            setConfig.setName(name);
            config.addSetConfig(setConfig);
            return setElement(name, config.getSetConfig(name));
        }

        throw new IllegalArgumentException(
                "Unsupported Hazelcast cache definition type: " + definition.getClass().getName()
        );
    }

    private void materialize(
            HazelcastInstance instance,
            HazelcastElementDefinition element
    ) {
        switch (element.type()) {
            case MAP -> instance.getMap(element.name());
            case MULTI_MAP -> instance.getMultiMap(element.name());
            case REPLICATED_MAP -> instance.getReplicatedMap(element.name());
            case QUEUE -> instance.getQueue(element.name());
            case TOPIC -> instance.getTopic(element.name());
            case LIST -> instance.getList(element.name());
            case SET -> instance.getSet(element.name());
        }
    }

    private String normalizeName(String name, HazelcastElementType type) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hazelcast " + type + " configuration name must not be blank");
        }
        return name.trim();
    }

    private HazelcastElementDefinition mapElement(String name, MapConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.MAP,
                name,
                configText(
                        "ttlSeconds", config.getTimeToLiveSeconds(),
                        "maxIdleSeconds", config.getMaxIdleSeconds(),
                        "backupCount", config.getBackupCount(),
                        "asyncBackupCount", config.getAsyncBackupCount(),
                        "statisticsEnabled", config.isStatisticsEnabled(),
                        "evictionSize", evictionSize(config),
                        "evictionMaxSizePolicy", evictionMaxSizePolicy(config)
                )
        );
    }

    private HazelcastElementDefinition multiMapElement(String name, MultiMapConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.MULTI_MAP,
                name,
                configText(
                        "backupCount", config.getBackupCount(),
                        "asyncBackupCount", config.getAsyncBackupCount(),
                        "statisticsEnabled", config.isStatisticsEnabled()
                )
        );
    }

    private HazelcastElementDefinition replicatedMapElement(String name, ReplicatedMapConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.REPLICATED_MAP,
                name,
                configText(
                        "statisticsEnabled", config.isStatisticsEnabled()
                )
        );
    }

    private HazelcastElementDefinition queueElement(String name, QueueConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.QUEUE,
                name,
                configText(
                        "backupCount", config.getBackupCount(),
                        "asyncBackupCount", config.getAsyncBackupCount(),
                        "statisticsEnabled", config.isStatisticsEnabled()
                )
        );
    }

    private HazelcastElementDefinition topicElement(String name, TopicConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.TOPIC,
                name,
                configText(
                        "statisticsEnabled", config.isStatisticsEnabled()
                )
        );
    }

    private HazelcastElementDefinition listElement(String name, ListConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.LIST,
                name,
                configText(
                        "backupCount", config.getBackupCount(),
                        "asyncBackupCount", config.getAsyncBackupCount(),
                        "statisticsEnabled", config.isStatisticsEnabled()
                )
        );
    }

    private HazelcastElementDefinition setElement(String name, SetConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.SET,
                name,
                configText(
                        "backupCount", config.getBackupCount(),
                        "asyncBackupCount", config.getAsyncBackupCount(),
                        "statisticsEnabled", config.isStatisticsEnabled()
                )
        );
    }

    private Integer evictionSize(MapConfig config) {
        EvictionConfig evictionConfig = config.getEvictionConfig();
        return evictionConfig == null ? null : evictionConfig.getSize();
    }

    private String evictionMaxSizePolicy(MapConfig config) {
        EvictionConfig evictionConfig = config.getEvictionConfig();
        return evictionConfig == null || evictionConfig.getMaxSizePolicy() == null
                ? null
                : evictionConfig.getMaxSizePolicy().name();
    }

    private String configText(Object... keyValues) {
        if (keyValues == null || keyValues.length == 0) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int index = 0; index + 1 < keyValues.length; index += 2) {
            Object key = keyValues[index];
            Object value = keyValues[index + 1];
            if (key == null || value == null) {
                continue;
            }
            if (!result.isEmpty()) {
                result.append(',');
            }
            result.append(key).append('=').append(value);
        }
        return result.isEmpty() ? null : result.toString();
    }
}
