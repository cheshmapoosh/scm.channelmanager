-- 14020424
CREATE TABLE REF.TBL_SCM_SERVICE_PROVIDER (
                                              SERVICE_PROVIDER_ID VARCHAR(36) NOT NULL,
                                              CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              CREATOR VARCHAR(255),
                                              LAST_EDITOR VARCHAR(255),
                                              NAME VARCHAR(255),
                                              TITLE VARCHAR(255),
                                              COMPONENT_NAME VARCHAR(255),
                                              VALUE VARCHAR(255),
                                              PRIMARY KEY (SERVICE_PROVIDER_ID)
)


INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CREATOR, LAST_EDITOR, NAME, TITLE, COMPONENT_NAME, VALUE)
VALUES ('c3a52c86-8f97-4c9d-9c01-877be1242d92', 'Reza Jamshidi', 'Reza Jamshidi', 'Nab Rest', 'همراه', 'NAB', '');