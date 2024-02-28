-------------------------------------------------------------------------------------------------------

CREATE TABLE REF.TBL_SNT_MESSAGE_TEMPLATE
(
    MESSAGE_TEMPLATE_ID    DECIMAL(22) GENERATED ALWAYS AS IDENTITY NOT NULL,
    CREATOR                VARCHAR(255),
    LAST_EDITOR            VARCHAR(255),
    CREATE_DATE            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    TRY_COUNT              INTEGER,
    MAX_MINUTES_EXPIRATION INTEGER,
    LAST_EDIT_DATE         TIMESTAMP,
    CODE                   VARCHAR(255),
    TITLE                  VARCHAR(255),
    BODY                   VARCHAR(255),
    IS_SYSTEMIC            SMALLINT  DEFAULT 0                      NOT NULL,
    CONSTRAINT PK_TBL_SNT_MESSAGE_TEMPLATE PRIMARY KEY (MESSAGE_TEMPLATE_ID)
);

---------------------------------------------------------------------------------------------------

CREATE TABLE REF.TBL_SNT_NOTIFICATION_QUEUE
(
    NOTIFICATION_QUEUE_ID VARCHAR(255) NOT NULL,
    MEDIA                 VARCHAR(255),
    MESSAGE_TEMPLATE_ID   DECIMAL(22),
    MESSAGE               VARCHAR(255),
    RECIPIENT             VARCHAR(255),
    STATUS                VARCHAR(255),
    EXPIRATION            TIMESTAMP,
    TRY_COUNT             INTEGER,
    CREATOR               VARCHAR(255),
    LAST_EDITOR           VARCHAR(255),
    CREATE_DATE           TIMESTAMP,
    LAST_EDIT_DATE        TIMESTAMP,
    CONSTRAINT PK_TBL_SNT_NOTIFICATION_QUEUE PRIMARY KEY (NOTIFICATION_QUEUE_ID)
);

-------------------------------------------------------------------------------------------------
-- CREATE TABLE REF.TBL_SNT_TEMPLATE_CODE
-- (
--     MESSAGE_TEMPLATE_ID DECIMAL(22) GENERATED ALWAYS AS IDENTITY NOT NULL,
--     CREATOR             VARCHAR(255),
--     LAST_EDITOR         VARCHAR(255),
--     CREATE_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
--     LAST_EDIT_DATE      TIMESTAMP,
--     CODE                VARCHAR(255),
--     CONSTRAINT PK_TBL_SNT_MESSAGE_TEMPLATE PRIMARY KEY (MESSAGE_TEMPLATE_ID)
-- );