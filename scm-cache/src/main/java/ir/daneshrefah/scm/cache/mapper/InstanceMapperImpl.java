package ir.daneshrefah.scm.cache.mapper;

import com.hazelcast.config.*;
import ir.daneshrefah.scm.cache.domain.config.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InstanceMapperImpl implements InstanceMapper {

    @Override
    public SetConfig mapToSetConfig(SetCacheConfigEntity setCacheConfigEntity) {
        SetConfig setConfig = new SetConfig();
        if (Objects.nonNull(setCacheConfigEntity.getInstanceName())) {
            setConfig.setName(setCacheConfigEntity.getInstanceName());
        }
        if (Objects.nonNull(setCacheConfigEntity.getStatisticsEnabled())) {
            setConfig.setStatisticsEnabled(setCacheConfigEntity.getStatisticsEnabled());
        }
        if (Objects.nonNull(setCacheConfigEntity.getMaxSize())) {
            setConfig.setMaxSize(setCacheConfigEntity.getMaxSize());
        }
        if (Objects.nonNull(setCacheConfigEntity.getBackupCount())) {
            setConfig.setBackupCount(setCacheConfigEntity.getBackupCount());
        }
        if (Objects.nonNull(setCacheConfigEntity.getAsyncBackupCount())) {
            setConfig.setAsyncBackupCount(setCacheConfigEntity.getAsyncBackupCount());
        }
        return setConfig;
    }

    @Override
    public ListConfig mapToListConfig(ListCacheConfigEntity listCacheConfigEntity) {
        ListConfig listConfig = new ListConfig();
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig();
        boolean hasMergePolicyConfig = false;
        if (Objects.nonNull(listCacheConfigEntity.getInstanceName())) {
            listConfig.setName(listCacheConfigEntity.getInstanceName());
        }
        if (Objects.nonNull(listCacheConfigEntity.getBackupCount())) {
            listConfig.setBackupCount(listCacheConfigEntity.getBackupCount());
        }
        if (Objects.nonNull(listCacheConfigEntity.getAsyncBackupCount())) {
            listConfig.setAsyncBackupCount(listCacheConfigEntity.getAsyncBackupCount());
        }
        if (Objects.nonNull(listCacheConfigEntity.getMaxSize())) {
            listConfig.setMaxSize(listCacheConfigEntity.getMaxSize());
        }
        if (Objects.nonNull(listCacheConfigEntity.getStatisticsEnabled())) {
            listConfig.setStatisticsEnabled(listCacheConfigEntity.getStatisticsEnabled());
        }
        if (Objects.nonNull(listCacheConfigEntity.getMergePolicyBatchSize())) {
            mergePolicyConfig.setBatchSize(listCacheConfigEntity.getMergePolicyBatchSize());
            hasMergePolicyConfig = true;
        }
        if (hasMergePolicyConfig) {
            listConfig.setMergePolicyConfig(mergePolicyConfig);
        }
        return listConfig;
    }

    @Override
    public TopicConfig mapToTopicConfig(TopicCacheConfigEntity topicCacheConfigEntity) {
        TopicConfig topicConfig = new TopicConfig();
        if (Objects.nonNull(topicCacheConfigEntity.getInstanceName())) {
            topicConfig.setName(topicCacheConfigEntity.getInstanceName());
        }
        if (Objects.nonNull(topicCacheConfigEntity.getTopicGlobalOrderingEnabled())) {
            topicConfig.setGlobalOrderingEnabled(topicCacheConfigEntity.getTopicGlobalOrderingEnabled());
        }
        if (Objects.nonNull(topicCacheConfigEntity.getStatisticsEnabled())) {
            topicConfig.setStatisticsEnabled(topicCacheConfigEntity.getStatisticsEnabled());
        }
        if (Objects.nonNull(topicCacheConfigEntity.getTopicMultiThreadingEnabled())) {
            topicConfig.setMultiThreadingEnabled(topicCacheConfigEntity.getTopicMultiThreadingEnabled());
        }
        return topicConfig;
    }

    @Override
    public QueueConfig mapToQueueConfig(QueueCacheConfigEntity queueCacheConfigEntity) {
        QueueConfig queueConfig = new QueueConfig();
        if (Objects.nonNull(queueCacheConfigEntity.getInstanceName())) {
            queueConfig.setName(queueCacheConfigEntity.getInstanceName());
        }
        if (Objects.nonNull(queueCacheConfigEntity.getBackupCount())) {
            queueConfig.setBackupCount(queueCacheConfigEntity.getBackupCount());
        }
        if (Objects.nonNull(queueCacheConfigEntity.getAsyncBackupCount())) {
            queueConfig.setAsyncBackupCount(queueCacheConfigEntity.getAsyncBackupCount());
        }
        if (Objects.nonNull(queueCacheConfigEntity.getMaxSize())) {
            queueConfig.setMaxSize(queueCacheConfigEntity.getMaxSize());
        }
        if (Objects.nonNull(queueCacheConfigEntity.getPriorityComparatorClassName())) {
            queueConfig.setPriorityComparatorClassName(queueCacheConfigEntity.getPriorityComparatorClassName());
        }
        if (Objects.nonNull(queueCacheConfigEntity.getEmptyQueueTtl())) {
            queueConfig.setEmptyQueueTtl(queueCacheConfigEntity.getEmptyQueueTtl());
        }
        return queueConfig;
    }

    @Override
    public ReplicatedMapConfig mapToReplicatedConfig(ReplicatedMapCacheConfigEntity replicatedMapCacheConfigEntity) {
        ReplicatedMapConfig mapConfig = new ReplicatedMapConfig();
        if (Objects.nonNull(replicatedMapCacheConfigEntity.getInstanceName())) {
            mapConfig.setName(replicatedMapCacheConfigEntity.getInstanceName());
        }
        if (Objects.nonNull(replicatedMapCacheConfigEntity.getAsyncFillUpEnabled())) {
            mapConfig.setAsyncFillup(replicatedMapCacheConfigEntity.getAsyncFillUpEnabled());
        }
        if (Objects.nonNull(replicatedMapCacheConfigEntity.getInMemoryFormat())) {
            mapConfig.setInMemoryFormat(InMemoryFormat.getById(replicatedMapCacheConfigEntity.getInMemoryFormat()));
        }
        if (Objects.nonNull(replicatedMapCacheConfigEntity.getStatisticsEnabled())) {
            mapConfig.setStatisticsEnabled(replicatedMapCacheConfigEntity.getStatisticsEnabled());
        }
        if (Objects.nonNull(replicatedMapCacheConfigEntity.getSplitBrainProtectionName())) {
            mapConfig.setSplitBrainProtectionName(replicatedMapCacheConfigEntity.getSplitBrainProtectionName());
        }
        return mapConfig;
    }

    @Override
    public MultiMapConfig mapToMultiMapConfig(MultiMapCacheConfigEntity multiMapCacheConfigEntity) {
        MultiMapConfig mapConfig = new MultiMapConfig();
        if (Objects.nonNull(multiMapCacheConfigEntity.getInstanceName())) {
            mapConfig.setName(multiMapCacheConfigEntity.getInstanceName());
        }
        if (Objects.nonNull(multiMapCacheConfigEntity.getBinaryEnabled())) {
            mapConfig.setBinary(multiMapCacheConfigEntity.getBinaryEnabled());
        }
        if (Objects.nonNull(multiMapCacheConfigEntity.getStatisticsEnabled())) {
            mapConfig.setStatisticsEnabled(multiMapCacheConfigEntity.getStatisticsEnabled());
        }
        if (Objects.nonNull(multiMapCacheConfigEntity.getBackupCount())) {
            mapConfig.setBackupCount(multiMapCacheConfigEntity.getBackupCount());
        }
        if (Objects.nonNull(multiMapCacheConfigEntity.getAsyncBackupCount())) {
            mapConfig.setAsyncBackupCount(multiMapCacheConfigEntity.getAsyncBackupCount());
        }
        if (Objects.nonNull(multiMapCacheConfigEntity.getValueCollectionType())) {
            mapConfig.setValueCollectionType(multiMapCacheConfigEntity.getValueCollectionType());
        }
        if (Objects.nonNull(multiMapCacheConfigEntity.getSplitBrainProtectionName())) {
            mapConfig.setSplitBrainProtectionName(multiMapCacheConfigEntity.getSplitBrainProtectionName());
        }
        return mapConfig;
    }

    @Override
    public MapConfig mapToMapConfig(MapCacheConfigEntity mapCacheConfig) {
        MapConfig mapConfig = new MapConfig();
        EvictionConfig evictionConfig = new EvictionConfig();
        boolean hasEvictionConfig = false;
        mapConfig.setName(mapCacheConfig.getInstanceName());
        if (Objects.nonNull(mapCacheConfig.getTimeToLiveSeconds())) {
            mapConfig.setTimeToLiveSeconds(mapCacheConfig.getTimeToLiveSeconds());
        }
        if (Objects.nonNull(mapCacheConfig.getBackupCount())) {
            mapConfig.setBackupCount(mapCacheConfig.getBackupCount());
        }
        if (Objects.nonNull(mapCacheConfig.getMaxIdleSeconds())) {
            mapConfig.setMaxIdleSeconds(mapCacheConfig.getMaxIdleSeconds());
        }
        if (Objects.nonNull(mapCacheConfig.getAsyncBackupCount())) {
            mapConfig.setAsyncBackupCount(mapCacheConfig.getAsyncBackupCount());
        }
        if (Objects.nonNull(mapCacheConfig.getStatisticsEnabled())) {
            mapConfig.setStatisticsEnabled(mapCacheConfig.getStatisticsEnabled());
            mapConfig.setPerEntryStatsEnabled(mapCacheConfig.getStatisticsEnabled());
        }
        if (Objects.nonNull(mapCacheConfig.getEvictionSize())) {
            evictionConfig.setSize(mapCacheConfig.getEvictionSize());
            hasEvictionConfig = true;
        }
        if (Objects.nonNull(mapCacheConfig.getEvictionMaxSizePolicy())) {
            evictionConfig.setMaxSizePolicy(MaxSizePolicy.getById(mapCacheConfig.getEvictionMaxSizePolicy()));
            hasEvictionConfig = true;
        }
        if (hasEvictionConfig) {
            mapConfig.setEvictionConfig(evictionConfig);
        }
        return mapConfig;
    }
}
