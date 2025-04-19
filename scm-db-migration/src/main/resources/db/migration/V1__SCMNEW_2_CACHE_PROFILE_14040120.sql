create table REF.TBL_SCM_CACHE_PROFILE
(
    ID                       DECIMAL(19) generated always as identity,
    SERVICE_ID               VARCHAR(36)
        references TBL_SCM_SERVICE not null ,
    CACHE_TEMPLATE_ID        DECIMAL(19)
        references TBL_CHE_INSTANCE_CONFIG not null ,
    TIME_TO_LIVE             DECIMAL(8),
    MAX_IDLE                 DECIMAL(8),
    IN_MEMORY_FORMAT         SMALLINT NOT NULL,
    INVALID_ON_CHANGE        SMALLINT,
    CACHE_LOCAL_ENTRIES      SMALLINT,
    EVICTION_SIZE            SMALLINT,
    EVICTION_POLICY          SMALLINT,
    EVICTION_MAX_SIZE_POLICY SMALLINT,
    CACHE_CONFIG             VARCHAR(10485),
    JSON_KEY                 VARCHAR(10485),
    ACTIVE                   SMALLINT not null
);