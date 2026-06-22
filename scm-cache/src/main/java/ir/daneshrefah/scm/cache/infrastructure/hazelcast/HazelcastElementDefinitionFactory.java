package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.ListConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MultiMapConfig;
import com.hazelcast.config.QueueConfig;
import com.hazelcast.config.ReplicatedMapConfig;
import com.hazelcast.config.SetConfig;
import com.hazelcast.config.TopicConfig;
import org.springframework.stereotype.Component;

@Component
public class HazelcastElementDefinitionFactory {
    public HazelcastElementDefinition map(String name, MapConfig config) {
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

    public HazelcastElementDefinition multiMap(String name, MultiMapConfig config) {
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

    public HazelcastElementDefinition replicatedMap(String name, ReplicatedMapConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.REPLICATED_MAP,
                name,
                configText("statisticsEnabled", config.isStatisticsEnabled())
        );
    }

    public HazelcastElementDefinition queue(String name, QueueConfig config) {
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

    public HazelcastElementDefinition topic(String name, TopicConfig config) {
        return new HazelcastElementDefinition(
                HazelcastElementType.TOPIC,
                name,
                configText("statisticsEnabled", config.isStatisticsEnabled())
        );
    }

    public HazelcastElementDefinition list(String name, ListConfig config) {
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

    public HazelcastElementDefinition set(String name, SetConfig config) {
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
