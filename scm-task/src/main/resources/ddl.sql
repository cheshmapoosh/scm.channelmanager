create table REF.TBL_PRC_PROCESS_INSTANCE
(
    PROCESS_ID               decimal(19) primary key generated always as identity,
    CONFIRM_USER_ID          int
        constraint TBL_PRC_PROCESS_INSTANCE_USER_ID_FK
            references REF.USER (USER_ID),
    ACCOUNT_NO               varchar(55) not null,
    TRANSACTION_DATA         CLOB        not null,
    LAST_MESSAGE_SEQUENCE_ID varchar(32),
    PROCESS_CODE             varchar(55) not null,
    AMOUNT                   decimal(19),
    CORRELATION_ID           varchar(40),
    DESTINATION              varchar(200),
    DESCRIPTION              varchar(2048),
    STATUS                   smallint    not null,
    ARCHIVE_NO               int     not null,
    CREATE_BY                int         not null,
    UPDATE_BY                int,
    CREATE_AT                timestamp   not null,
    UPDATE_AT                timestamp
);

create table REF.TBL_PRC_TASKS
(
    TASK_ID    decimal(19) primary key generated always as identity,
    PROCESS_ID decimal(19) not null
        constraint TBL_PRC_TASKS_PROCESS_ID_FK
            references REF.TBL_PRC_PROCESS_INSTANCE (PROCESS_ID),
    USER_ID    int         not null
        constraint TBL_PRC_TASKS_USER_ID_FK
            references REF.USER (USER_ID),
    FULL_NAME  varchar(255),
    STATUS     smallint    not null,
    IS_GLOBAL  smallint,
    ARCHIVE_NO int     not null,
    CREATE_BY  int         not null,
    CREATE_AT  timestamp   not null,
    UPDATE_AT  timestamp
);

create table REF.TBL_PRC_PROCESS_INSTANCE_WATCHER
(
    PROCESS_INSTANCE_WATCHER_ID decimal(19) primary key generated always as identity,
    PROCESS_ID                  decimal(19) not null
        constraint TBL_PRC_TASKS_PROCESS_ID_FK
            references REF.TBL_PRC_PROCESS_INSTANCE (PROCESS_ID),
    USER_ID                     int         not null
        constraint TBL_PRC_TASKS_USER_ID_FK
            references REF.USER (USER_ID),
    CREATE_BY                   int         not null,
    CREATE_AT                   timestamp   not null
);

create table REF.TBL_PRC_TASKS_LOG
(
    TASK_LOG_ID       decimal(19) primary key generated always as identity,
    TASK_ID           decimal(19)  not null
        constraint TBL_PRC_TASKS_LOG_FK
            references REF.TBL_PRC_TASKS (TASK_ID),
    LAST_CHANNEL_CODE varchar(255) not null,
    STATUS            smallint     not null,
    CREATE_BY         int          not null,
    CREATE_AT         timestamp default CURRENT TIMESTAMP
);

create table REF.TBL_PRC_PROCESS_TASK_DEFINITIONS
(
    DEFINITION_ID                   decimal(19) primary key generated always as identity,
    PROCESS_NAME                    varchar(255) not null,
    EXECUTION_METHOD                smallint     not null,
    DEFINITION_TYPE                 smallint     not null,
    USER_ACCESS_SECOND_AUTH         smallint     not null default 0,
    CONFIRM_USER_ACCESS_SECOND_AUTH smallint     not null default 0,
    USER_ALLOWED_CANCEL             smallint     not null default 0,
    CONFIRM_USER_ALLOWED_CANCEL     smallint     not null default 0,
    PROCESS_CODE                    smallint     not null,
    OTP_REASON                      smallint,
    CREATE_BY                       varchar(255) not null,
    UPDATE_BY                       varchar(255),
    CREATE_AT                       timestamp             default CURRENT TIMESTAMP,
    CONSTRAINT unique_process_definition UNIQUE (EXECUTION_METHOD, DEFINITION_TYPE, PROCESS_CODE)
);

