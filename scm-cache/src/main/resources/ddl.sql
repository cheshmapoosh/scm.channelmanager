CREATE TABLE REF.TBL_CHE_INSTANCE_CONFIG
(
    INSTANCE_ID                    VARCHAR(36)  NOT NULL PRIMARY KEY,
    INSTANCE_TYPE                  VARCHAR(20),
    INSTANCE_NAME                  VARCHAR(128) NOT NULL,
    STATISTICS_ENABLED             SMALLINT,
    TIME_TO_LIVE_SECONDS           INTEGER,
    MAX_IDLE_SECONDS               INTEGER,
    MAX_SIZE                       INTEGER,
    BACKUP_COUNT                   INTEGER,
    ASYNC_BACKUP_COUNT             INTEGER,
    EVICTION_SIZE                  INTEGER,
    EVICTION_MAX_SIZE_POLICY       INTEGER,
    MERGE_POLICY_BATCH_SIZE        INTEGER,
    BINARY_ENABLED                 SMALLINT,
    SPLIT_BRAIN_PROTECTION_NAME    VARCHAR(64),
    VALUE_COLLECTION_TYPE          varchar(64),
    PRIORITY_COMPARATOR_CLASS_NAME VARCHAR(128),
    EMPTY_QUEUE_TTL                INTEGER,
    ASYNC_FILL_UP_ENABLED          SMALLINT,
    IN_MEMORY_FORMAT               INTEGER,
    TOPIC_GLOBAL_ORDERING_ENABLED  SMALLINT,
    TOPIC_MULTI_THREADING_ENABLED  SMALLINT
);

INSERT INTO REF.TBL_CHE_INSTANCE_CONFIG (INSTANCE_ID, INSTANCE_TYPE, INSTANCE_NAME, STATISTICS_ENABLED,
                                         TIME_TO_LIVE_SECONDS, MAX_IDLE_SECONDS, MAX_SIZE, BACKUP_COUNT,
                                         ASYNC_BACKUP_COUNT, EVICTION_SIZE, EVICTION_MAX_SIZE_POLICY,
                                         MERGE_POLICY_BATCH_SIZE, BINARY_ENABLED, SPLIT_BRAIN_PROTECTION_NAME,
                                         VALUE_COLLECTION_TYPE, PRIORITY_COMPARATOR_CLASS_NAME, EMPTY_QUEUE_TTL,
                                         ASYNC_FILL_UP_ENABLED, IN_MEMORY_FORMAT, TOPIC_GLOBAL_ORDERING_ENABLED,
                                         TOPIC_MULTI_THREADING_ENABLED)
VALUES ('e0a16460-8cfa-11ee-b9d1-0242ac120002', 'MAP', 'ttl-idle-default', 1, 60, 120, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null);
INSERT INTO REF.TBL_CHE_INSTANCE_CONFIG (INSTANCE_ID, INSTANCE_TYPE, INSTANCE_NAME, STATISTICS_ENABLED,
                                         TIME_TO_LIVE_SECONDS, MAX_IDLE_SECONDS, MAX_SIZE, BACKUP_COUNT,
                                         ASYNC_BACKUP_COUNT, EVICTION_SIZE, EVICTION_MAX_SIZE_POLICY,
                                         MERGE_POLICY_BATCH_SIZE, BINARY_ENABLED, SPLIT_BRAIN_PROTECTION_NAME,
                                         VALUE_COLLECTION_TYPE, PRIORITY_COMPARATOR_CLASS_NAME, EMPTY_QUEUE_TTL,
                                         ASYNC_FILL_UP_ENABLED, IN_MEMORY_FORMAT, TOPIC_GLOBAL_ORDERING_ENABLED,
                                         TOPIC_MULTI_THREADING_ENABLED)
VALUES ('01b0ebb2-8cfb-11ee-b9d1-0242ac120002', 'MAP', 'default', 1, null, null, null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null);
