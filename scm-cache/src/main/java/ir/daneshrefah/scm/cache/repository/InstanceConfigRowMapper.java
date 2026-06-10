package ir.daneshrefah.scm.cache.repository;

import ir.daneshrefah.scm.cache.domain.InstanceType;
import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.ListCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.MapCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.MultiMapCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.QueueCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.ReplicatedMapCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.SetCacheConfigEntity;
import ir.daneshrefah.scm.cache.domain.config.TopicCacheConfigEntity;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

@Component
public class InstanceConfigRowMapper
        implements RowMapper<InstanceConfigEntity> {

    private static final String INSTANCE_ID = "INSTANCE_ID";
    private static final String INSTANCE_TYPE = "INSTANCE_TYPE";
    private static final String INSTANCE_NAME = "INSTANCE_NAME";
    private static final String STATISTICS_ENABLED =
            "STATISTICS_ENABLED";

    private static final String TTL_SECONDS = "TTL_SECONDS";
    private static final String BACKUP_COUNT = "BACKUP_COUNT";
    private static final String MAX_IDLE_SECONDS =
            "MAX_IDLE_SECONDS";
    private static final String EVIC_SIZE = "EVIC_SIZE";
    private static final String EVIC_MAX_SIZE_POLICY =
            "EVIC_MAX_SIZE_POLICY";
    private static final String ASYNC_BACKUP_COUNT =
            "ASYNC_BACKUP_COUNT";

    private static final String MAX_SIZE = "MAX_SIZE";
    private static final String MERGE_POLICY_BATCH_SIZE =
            "MERGE_POLICY_BATCH_SIZE";

    private static final String BINARY_ENABLED = "BINARY_ENABLED";
    private static final String SPLIT_BRAIN_PROTECTION_NAME =
            "SPLIT_BRAIN_PROTECTION_NAME";
    private static final String VALUE_COLLECTION_TYPE =
            "VALUE_COLLECTION_TYPE";

    private static final String ASYNC_FILL_UP_ENABLED =
            "ASYNC_FILL_UP_ENABLED";
    private static final String IN_MEMORY_FORMAT =
            "IN_MEMORY_FORMAT";

    private static final String PRIORITY_COMP_CLASS_NAME =
            "PRIORITY_COMP_CLASS_NAME";
    private static final String EMPTY_QUEUE_TTL =
            "EMPTY_QUEUE_TTL";

    private static final String TOPIC_GLOBAL_ORD_ENABLED =
            "TOPIC_GLOBAL_ORD_ENABLED";
    private static final String TOPIC_MULTI_THRD_ENABLED =
            "TOPIC_MULTI_THRD_ENABLED";

    @Override
    public InstanceConfigEntity mapRow(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {
        String instanceType = requiredInstanceType(
                resultSet,
                rowNumber
        );

        InstanceConfigEntity entity = switch (instanceType) {
            case InstanceType.MAP ->
                    mapMapConfig(resultSet);

            case InstanceType.LIST ->
                    mapListConfig(resultSet);

            case InstanceType.MULTIMAP ->
                    mapMultiMapConfig(resultSet);

            case InstanceType.REPLICATED_MAP ->
                    mapReplicatedMapConfig(resultSet);

            case InstanceType.QUEUE ->
                    mapQueueConfig(resultSet);

            case InstanceType.SET ->
                    mapSetConfig(resultSet);

            case InstanceType.TOPIC ->
                    mapTopicConfig(resultSet);

            default -> throw new DataRetrievalFailureException(
                    "Unsupported cache instance type '%s' at row %d"
                            .formatted(instanceType, rowNumber)
            );
        };

        mapCommonFields(resultSet, entity);
        return entity;
    }

    private MapCacheConfigEntity mapMapConfig(
            ResultSet resultSet
    ) throws SQLException {
        MapCacheConfigEntity entity =
                new MapCacheConfigEntity();

        entity.setTimeToLiveSeconds(
                nullableInteger(resultSet, TTL_SECONDS)
        );
        entity.setBackupCount(
                nullableInteger(resultSet, BACKUP_COUNT)
        );
        entity.setMaxIdleSeconds(
                nullableInteger(resultSet, MAX_IDLE_SECONDS)
        );
        entity.setEvictionSize(
                nullableInteger(resultSet, EVIC_SIZE)
        );
        entity.setEvictionMaxSizePolicy(
                nullableInteger(
                        resultSet,
                        EVIC_MAX_SIZE_POLICY
                )
        );
        entity.setAsyncBackupCount(
                nullableInteger(
                        resultSet,
                        ASYNC_BACKUP_COUNT
                )
        );

        return entity;
    }

    private ListCacheConfigEntity mapListConfig(
            ResultSet resultSet
    ) throws SQLException {
        ListCacheConfigEntity entity =
                new ListCacheConfigEntity();

        entity.setMaxSize(
                nullableInteger(resultSet, MAX_SIZE)
        );
        entity.setBackupCount(
                nullableInteger(resultSet, BACKUP_COUNT)
        );
        entity.setAsyncBackupCount(
                nullableInteger(
                        resultSet,
                        ASYNC_BACKUP_COUNT
                )
        );
        entity.setMergePolicyBatchSize(
                nullableInteger(
                        resultSet,
                        MERGE_POLICY_BATCH_SIZE
                )
        );

        return entity;
    }

    private MultiMapCacheConfigEntity mapMultiMapConfig(
            ResultSet resultSet
    ) throws SQLException {
        MultiMapCacheConfigEntity entity =
                new MultiMapCacheConfigEntity();

        entity.setBinaryEnabled(
                nullableBoolean(resultSet, BINARY_ENABLED)
        );
        entity.setBackupCount(
                nullableInteger(resultSet, BACKUP_COUNT)
        );
        entity.setAsyncBackupCount(
                nullableInteger(
                        resultSet,
                        ASYNC_BACKUP_COUNT
                )
        );
        entity.setSplitBrainProtectionName(
                resultSet.getString(
                        SPLIT_BRAIN_PROTECTION_NAME
                )
        );
        entity.setValueCollectionType(
                resultSet.getString(VALUE_COLLECTION_TYPE)
        );

        return entity;
    }

    private ReplicatedMapCacheConfigEntity
    mapReplicatedMapConfig(
            ResultSet resultSet
    ) throws SQLException {
        ReplicatedMapCacheConfigEntity entity =
                new ReplicatedMapCacheConfigEntity();

        entity.setAsyncFillUpEnabled(
                nullableBoolean(
                        resultSet,
                        ASYNC_FILL_UP_ENABLED
                )
        );
        entity.setInMemoryFormat(
                nullableInteger(
                        resultSet,
                        IN_MEMORY_FORMAT
                )
        );
        entity.setSplitBrainProtectionName(
                resultSet.getString(
                        SPLIT_BRAIN_PROTECTION_NAME
                )
        );

        return entity;
    }

    private QueueCacheConfigEntity mapQueueConfig(
            ResultSet resultSet
    ) throws SQLException {
        QueueCacheConfigEntity entity =
                new QueueCacheConfigEntity();

        entity.setBackupCount(
                nullableInteger(resultSet, BACKUP_COUNT)
        );
        entity.setAsyncBackupCount(
                nullableInteger(
                        resultSet,
                        ASYNC_BACKUP_COUNT
                )
        );
        entity.setMaxSize(
                nullableInteger(resultSet, MAX_SIZE)
        );
        entity.setPriorityComparatorClassName(
                resultSet.getString(
                        PRIORITY_COMP_CLASS_NAME
                )
        );
        entity.setEmptyQueueTtl(
                nullableInteger(
                        resultSet,
                        EMPTY_QUEUE_TTL
                )
        );

        return entity;
    }

    private SetCacheConfigEntity mapSetConfig(
            ResultSet resultSet
    ) throws SQLException {
        SetCacheConfigEntity entity =
                new SetCacheConfigEntity();

        entity.setMaxSize(
                nullableInteger(resultSet, MAX_SIZE)
        );
        entity.setBackupCount(
                nullableInteger(resultSet, BACKUP_COUNT)
        );
        entity.setAsyncBackupCount(
                nullableInteger(
                        resultSet,
                        ASYNC_BACKUP_COUNT
                )
        );

        return entity;
    }

    private TopicCacheConfigEntity mapTopicConfig(
            ResultSet resultSet
    ) throws SQLException {
        TopicCacheConfigEntity entity =
                new TopicCacheConfigEntity();

        entity.setTopicGlobalOrderingEnabled(
                nullableBoolean(
                        resultSet,
                        TOPIC_GLOBAL_ORD_ENABLED
                )
        );
        entity.setTopicMultiThreadingEnabled(
                nullableBoolean(
                        resultSet,
                        TOPIC_MULTI_THRD_ENABLED
                )
        );

        return entity;
    }

    private void mapCommonFields(
            ResultSet resultSet,
            InstanceConfigEntity entity
    ) throws SQLException {
        entity.setId(resultSet.getString(INSTANCE_ID));
        entity.setInstanceName(
                resultSet.getString(INSTANCE_NAME)
        );
        entity.setStatisticsEnabled(
                nullableBoolean(
                        resultSet,
                        STATISTICS_ENABLED
                )
        );
    }

    private String requiredInstanceType(
            ResultSet resultSet,
            int rowNumber
    ) throws SQLException {
        String value = resultSet.getString(INSTANCE_TYPE);

        if (value == null || value.isBlank()) {
            throw new DataRetrievalFailureException(
                    "INSTANCE_TYPE is missing at row " + rowNumber
            );
        }

        return value.trim().toUpperCase(Locale.ROOT);
    }

    private Integer nullableInteger(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {
        Object value = resultSet.getObject(columnName);

        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.valueOf(value.toString().trim());
        } catch (NumberFormatException exception) {
            throw new SQLException(
                    "Column '%s' does not contain a valid integer: %s"
                            .formatted(columnName, value),
                    exception
            );
        }
    }

    private Boolean nullableBoolean(
            ResultSet resultSet,
            String columnName
    ) throws SQLException {
        Object value = resultSet.getObject(columnName);

        if (value == null) {
            return null;
        }

        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }

        if (value instanceof Number number) {
            return number.intValue() != 0;
        }

        String normalized = value.toString()
                .trim()
                .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "1", "Y", "YES", "T", "TRUE" -> true;
            case "0", "N", "NO", "F", "FALSE" -> false;

            default -> throw new SQLException(
                    "Column '%s' does not contain a valid boolean: %s"
                            .formatted(columnName, value)
            );
        };
    }
}