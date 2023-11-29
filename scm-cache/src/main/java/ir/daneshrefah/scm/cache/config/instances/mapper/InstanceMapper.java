package ir.daneshrefah.scm.cache.config.instances.mapper;

import com.hazelcast.config.*;
import ir.daneshrefah.scm.cache.config.instances.domain.*;

public interface InstanceMapper {

    SetConfig mapToSetConfig(SetCacheConfigEntity setCacheConfigEntity);

    ListConfig mapToListConfig(ListCacheConfigEntity listCacheConfigEntity);

    TopicConfig mapToTopicConfig(TopicCacheConfigEntity topicCacheConfigEntity);

    QueueConfig mapToQueueConfig(QueueCacheConfigEntity queueCacheConfigEntity);

    ReplicatedMapConfig mapToReplicatedConfig(ReplicatedMapCacheConfigEntity replicatedMapCacheConfigEntity);

    MultiMapConfig mapToMultiMapConfig(MultiMapCacheConfigEntity multiMapCacheConfigEntity);

    MapConfig mapToMapConfig(MapCacheConfigEntity mapCacheConfigEntity);
}
