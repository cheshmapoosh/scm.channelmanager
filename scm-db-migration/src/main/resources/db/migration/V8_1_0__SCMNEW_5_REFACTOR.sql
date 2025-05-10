CREATE TABLE REF.TBL_SCM_GATEWAY_CHANNEL
(
    GATEWAY_CHANNEL_ID VARCHAR(36)  NOT NULL,
    NAME               VARCHAR(100) NOT NULL,
    CODE               VARCHAR(50)  NOT NULL,
    ACTIVE             SMALLINT     NOT NULL,
    DESCRIPTION        VARCHAR(255),
    CHANNEL_ID         SMALLINT     NOT NULL,
    PROTOCOL_TYPE      VARCHAR(20)  NOT NULL,
    HOST               VARCHAR(50),
    PORT               SMALLINT,
    PATH               VARCHAR(100),
    METADATA           VARCHAR(2048),
    CREATOR            VARCHAR(255),
    LAST_EDITOR        VARCHAR(255),
    CREATE_DATE        TIMESTAMP,
    LAST_EDIT_DATE     TIMESTAMP,
    VERSION            INTEGER,
    CONSTRAINT PK_GATEWAY_CHANNEL PRIMARY KEY (GATEWAY_CHANNEL_ID)
);

ALTER TABLE REF.TBL_SCM_GATEWAY_CHANNEL
    ADD CONSTRAINT UC_GTW_CHN_ON_PTC_CHN UNIQUE (CHANNEL_ID, PROTOCOL_TYPE);

ALTER TABLE REF.TBL_SCM_GATEWAY_CHANNEL
    ADD CONSTRAINT UC_GATEWAY_CHANNEL_CODE UNIQUE (CODE);

ALTER TABLE REF.TBL_SCM_GATEWAY_CHANNEL
    ADD CONSTRAINT FK_GTW_CHN_ON_CHN FOREIGN KEY (CHANNEL_ID) REFERENCES REF.CHANNEL (CHANNEL_ID);

------------------------------------------------------------------------------------------------------------------------
CREATE TABLE REF.TBL_SCM_CHANNEL_SERVICE_DEF
(
    CHANNEL_SERVICE_DEF_ID    VARCHAR(36) NOT NULL,
    CHANNEL_SERVICE_ACCESS_ID BIGINT      NOT NULL,
    GATEWAY_CHANNEL_CODE      VARCHAR(50) NOT NULL,
    TYPE                      VARCHAR(20) NOT NULL,
    METADATA                  VARCHAR(2048),
    CREATOR                   VARCHAR(255),
    LAST_EDITOR               VARCHAR(255),
    CREATE_DATE               TIMESTAMP,
    LAST_EDIT_DATE            TIMESTAMP,
    VERSION                   INTEGER,
    CONSTRAINT PK_CHANNEL_SERVICE_DEF PRIMARY KEY (CHANNEL_SERVICE_DEF_ID)
);

ALTER TABLE REF.TBL_SCM_CHANNEL_SERVICE_DEF
    ADD CONSTRAINT UC_GTW_ON_PTC_CHN UNIQUE (CHANNEL_SERVICE_ACCESS_ID, GATEWAY_CHANNEL_CODE, TYPE);

ALTER TABLE REF.TBL_SCM_CHANNEL_SERVICE_DEF
    ADD CONSTRAINT FK_CHN_SVC_DEF_ON_CHN_SVC FOREIGN KEY (CHANNEL_SERVICE_ACCESS_ID) REFERENCES REF.CHANNEL_SERVICE_ACCESS (CHANNEL_SERVICE_ACCESS_ID);

------------------------------------------------------------------------------------------------------------------------
CREATE TABLE REF.TBL_SCM_SERVICE_OPERATION
(
    SERVICE_OPERATION_ID VARCHAR(36) NOT NULL,
    ACTIVE               SMALLINT    NOT NULL,
    EB_SERVICE_ID        SMALLINT    NOT NULL,
    OPERATION_CODE       VARCHAR(50) NOT NULL,
    METADATA             VARCHAR(2048),
    CREATOR              VARCHAR(255),
    LAST_EDITOR          VARCHAR(255),
    CREATE_DATE          TIMESTAMP,
    LAST_EDIT_DATE       TIMESTAMP,
    VERSION              INTEGER,
    CONSTRAINT PK_SERVICE_OPERATION PRIMARY KEY (SERVICE_OPERATION_ID)
);

ALTER TABLE REF.TBL_SCM_SERVICE_OPERATION
    ADD CONSTRAINT UI_GTW_OPT_ON_SVC_CNS UNIQUE (EB_SERVICE_ID);

ALTER TABLE REF.TBL_SCM_SERVICE_OPERATION
    ADD CONSTRAINT FK_GTW_OPT_ON_SVC FOREIGN KEY (EB_SERVICE_ID) REFERENCES REF.EB_SERVICE (EB_SERVICE_ID);

------------------------------------------------------------------------------------------------------------------------
alter table REF.TBL_SCM_ERROR_MAPPING
    add VERSION INTEGER;

------------------------------------------------------------------------------------------------------------------------
alter table REF.TBL_SCM_SERVICE_PROVIDER
    add VERSION INTEGER;

------------------------------------------------------------------------------------------------------------------------
alter table REF.TBL_SCM_SERVICE_RESPONSE_CONDITION
    add VERSION INTEGER;

------------------------------------------------------------------------------------------------------------------------
alter table REF.TBL_SCM_PARAMETERS
    add VERSION INTEGER;

------------------------------------------------------------------------------------------------------------------------
alter table REF.TBL_SCM_DATASOURCE_CONDITION
    add VERSION INTEGER;

------------------------------------------------------------------------------------------------------------------------
alter table REF.EB_SERVICE
    add ROUTING_STRATEGY VARCHAR(20);

------------------------------------------------------------------------------------------------------------------------
alter table REF.EB_SERVICE
    add METADATA VARCHAR(2048);
