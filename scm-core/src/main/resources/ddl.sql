CREATE TABLE REF.TBL_SCM_CHANNEL (
                                     CHANNEL_ID VARCHAR(36) NOT NULL,
                                     CODE VARCHAR(255),
                                     TITLE VARCHAR(255),
                                     TERMINAL_ID VARCHAR(36) NOT NULL,
                                     PROTOCOL SMALLINT NOT NULL,
                                     CHANNEL_CLASS_NAME VARCHAR(255),
                                     METADATA VARCHAR(1000),
                                     CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CREATOR VARCHAR(255),
                                     LAST_EDITOR VARCHAR(255),
                                     FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
                                     PRIMARY KEY (CHANNEL_ID)
)
insert into TBL_SCM_CHANNEL (CHANNEL_ID, CODE, TITLE, PROTOCOL,
                             METADATA, CREATOR, LAST_EDITOR)
values ('a4d7637c-0855-4e81-a157-70b02742de24', 'SCM', 'مدیریت کانال', 2,
                            '{"contextPath": "/scm4dev", "port": 8082}', 'Reza Jamshidi', 'Reza Jamshidi');


CREATE TABLE REF.TBL_SCM_TERMINAL (
    --Definition
                                      TERMINAL_ID VARCHAR(36) NOT NULL,
                                      CODE VARCHAR(255),
                                      TITLE VARCHAR(255),
    --Attribute
                                      STATUS SMALLINT NOT NULL DEFAULT 1,
                                      SUPPORT_CHECK_AUTHENTICATION SMALLINT DEFAULT 1,
                                      SUPPORT_CHECK_SECOND_AUTHENTICATION SMALLINT DEFAULT 1,
                                      SUPPORT_CHECK_SERVICE_ACCESS SMALLINT DEFAULT 1,
                                      SUPPORT_CHECK_ASSET_ACCESS SMALLINT DEFAULT 1,
    --Versioning
                                      CREATE_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
                                      LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
                                      CREATOR VARCHAR(255),
                                      LAST_EDITOR VARCHAR(255),
    --Relation
                                      PRIMARY KEY (TERMINAL_ID)
);

insert into TBL_SCM_TERMINAL (TERMINAL_ID, CODE, TITLE, SUPPORT_CHECK_AUTHENTICATION, SUPPORT_CHECK_SECOND_AUTHENTICATION,
                              SUPPORT_CHECK_SERVICE_ACCESS, SUPPORT_CHECK_ASSET_ACCESS, CREATOR, LAST_EDITOR, STATUS)
values ('b9a79451-2141-40b6-98a0-72055a0042c5', 'SCM4DEV', 'محیط توسعه مدیریت کانال', 1, 1, 1, 0, 'Reza Jamshidi', 'Reza Jamshidi', 1);



CREATE TABLE REF.TBL_SCM_SERVICE_PROVIDER (
                                                       SERVICE_PROVIDER_ID VARCHAR(36) NOT NULL,
                                                       CODE VARCHAR(255),
                                                       TITLE VARCHAR(255),
                                                       PROVIDER_CLASS_NAME VARCHAR(255),
                                                       METADATA VARCHAR(255),
                                                       CUSTOMER_PROVIDED SMALLINT NOT NULL DEFAULT 0,
                                                       CUSTOMER_PROVIDER_CLASS_NAME VARCHAR(255),
                                                       CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                       LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                       CREATOR VARCHAR(255),
                                                       LAST_EDITOR VARCHAR(255),
                                                       PRIMARY KEY (EXTERNAL_SERVICE_PROVIDER_ID)
)
INSERT INTO REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER (EXTERNAL_SERVICE_PROVIDER_ID, CREATOR, LAST_EDITOR, CODE, TITLE, PROVIDER_CLASS_NAME, METADATA)
VALUES ('3ce3e10e-c3cd-49c7-ae5c-330a81e882d7', 'Reza Jamshidi', 'Reza Jamshidi', 'NAB', 'کر بانک رفاه', 'ir.daneshrefah.scm.plugin.nab.component.NabComponent', '{"baseUrl": "http://10.15.29.80/Service/"}');
INSERT INTO REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER (EXTERNAL_SERVICE_PROVIDER_ID, CREATOR, LAST_EDITOR, CODE, TITLE, PROVIDER_CLASS_NAME, METADATA)
VALUES ('ce027926-e5e1-4df5-b397-178dd41c87b8', 'Reza Jamshidi', 'Reza Jamshidi', 'MOCK', 'Mock', '', '');

EXTERNAL(1), JAVA(2), COMPOSITION(3), BPMN(4), PARENT(5);

CREATE TABLE REF.TBL_SCM_SERVICE (
    --Definition
                                     SERVICE_ID VARCHAR(36) NOT NULL,
                                     CODE VARCHAR(255) NOT NULL,
                                     TITLE VARCHAR(255),
                                     "ALIAS" VARCHAR(255),
                                     VERSION SMALLINT DEFAULT 1,
    --Attribute
                                     STATUS SMALLINT,
                                     IS_SYSTEMIC SMALLINT DEFAULT 0,
                                     REQUEST_JSON_SCHEMA VARCHAR(3000),
                                     RESPONSE_JSON_SCHEMA VARCHAR(2500),
                                     PARENT_SERVICE_ID VARCHAR(36),
                                     SERVICE_TYPE_CODE INTEGER,
                                     SERVICE_IMPLEMENTATION_TYPE_CODE INTEGER NOT NULL,
                                     IMPLEMENTATION_JAVA_CLASS_NAME VARCHAR(255),
                                     IMPLEMENTATION_EXTERNAL_SERVICE_PROVIDER_ID VARCHAR(36),
                                     IMPLEMENTATION_BPMN_CONTENT VARCHAR(1000),
                                     IMPLEMENTATION_COMPOSITION_TYPE_CODE INTEGER,
                                     CHECK_ACCESS_FIRST_AUTHENTICATION SMALLINT DEFAULT 0 NOT NULL,
                                     CHECK_ACCESS_SECOND_AUTHENTICATION SMALLINT DEFAULT 0 NOT NULL,
                                     CHECK_ACCESS_SERVICE SMALLINT DEFAULT 0 NOT NULL,
                                     CHECK_ACCESS_ASSET SMALLINT DEFAULT 0 NOT NULL,
                                     PROPERTY_NAME_AMOUNT VARCHAR(100),
                                     PROPERTY_NAME_ASSET VARCHAR(100),
    --Versioning
                                     CREATE_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
                                     LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
                                     CREATOR VARCHAR(255),
                                     LAST_EDITOR VARCHAR(255),
                                     METADATA VARCHAR(5500),
    --Relation
                                     CONSTRAINT CNST_UNIQUE_CODE UNIQUE (CODE),
                                     FOREIGN KEY (PARENT_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                     FOREIGN KEY (IMPLEMENTATION_EXTERNAL_SERVICE_PROVIDER_ID) REFERENCES REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER (EXTERNAL_SERVICE_PROVIDER_ID),
                                     PRIMARY KEY (SERVICE_ID)

);
insert into TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, IS_SYSTEMIC, SERVICE_TYPE_CODE, SERVICE_IMPLEMENTATION_TYPE_CODE,
                             CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                             CREATOR, LAST_EDITOR, STATUS)
values ('616d1811-0ad6-4f3c-80c0-47a003522d08', 'SVC_SERVICE_MANAGEMENT', 'مدیریت سرویس', '/service', 1, 1, 4, 5,
        1, 0, 0, 0, 'Reza Jamshidi', 'Reza Jamshidi', 1);
insert into TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, IS_SYSTEMIC, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                             SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_JAVA_CLASS_NAME,
                             CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION, STATUS, CREATOR,
                             LAST_EDITOR)
values ('790f7095-0102-44a7-9802-8d2530f09033', 'SVC_SERVICE_LIST', 'لیست سرویس', '', 1, 1, '616d1811-0ad6-4f3c-80c0-47a003522d08',
        1, 2, 'bean:serviceManagementService.serviceList', 1, 0, 1, 'Reza Jamshidi', 'Reza Jamshidi');
insert into TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, IS_SYSTEMIC, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                             SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_JAVA_CLASS_NAME,
                             CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION, STATUS, CREATOR,
                             LAST_EDITOR)
values ('d2220c12-30be-4305-9706-91d699826731', 'SVC_SERVICE_BY_CODE', 'یافتن سرویس با کد', '/code/{serviceCode}', 1, 1, '616d1811-0ad6-4f3c-80c0-47a003522d08',
        1, 2, 'bean:serviceManagementService.findServiceByCode', 1, 0, 1, 'Reza Jamshidi', 'Reza Jamshidi');


CREATE TABLE REF.TBL_SCM_SERVICE_RELATION (
                                              SERVICE_RELATION_ID VARCHAR(36) NOT NULL,
                                              SOURCE_SERVICE_ID VARCHAR(36) NOT NULL,
                                              ORDER INTEGER,
                                              RELATION_TYPE_CODE INTEGER,
                                              TARGET_SERVICE_ID VARCHAR(36) NOT NULL,
                                              TARGET_SERVICE_COMMIT_ID VARCHAR(36),
                                              TARGET_SERVICE_REVERSE_ID VARCHAR(36),
                                              CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                              CREATOR VARCHAR(255),
                                              LAST_EDITOR VARCHAR(255),
                                              FOREIGN KEY (SOURCE_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                              FOREIGN KEY (TARGET_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                              FOREIGN KEY (TARGET_SERVICE_COMMIT_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                              FOREIGN KEY (TARGET_SERVICE_REVERSE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                              PRIMARY KEY (SERVICE_RELATION_ID)
)
INSERT INTO "REF".TBL_SCM_SERVICE_RELATION
(SERVICE_RELATION_ID, SOURCE_SERVICE_ID, "ORDER", RELATION_TYPE_CODE,
 TARGET_SERVICE_ID, TARGET_SERVICE_TRANSFORMER_REQUEST_TYPE_CODE, TARGET_SERVICE_TRANSFORMER_REQUEST_METADATA, TARGET_SERVICE_TRANSFORMER_REQUEST_CLASS_NAME, TARGET_SERVICE_TRANSFORMER_RESPONSE_TYPE_CODE, TARGET_SERVICE_TRANSFORMER_RESPONSE_METADATA, TARGET_SERVICE_TRANSFORMER_RESPONSE_CLASS_NAME,
 TARGET_SERVICE_COMMIT_ID, TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_TYPE_CODE, TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_METADATA, TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_CLASS_NAME, TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_TYPE_CODE, TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_METADATA, TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_CLASS_NAME,
 TARGET_SERVICE_REVERSE_ID, TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_TYPE_CODE, TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_METADATA, TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_CLASS_NAME, TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_TYPE_CODE, TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_METADATA, TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_CLASS_NAME,
 CREATOR, LAST_EDITOR)
VALUES('7d2e1a3b-e894-4ea3-b01a-685361fd75e5', '2b6216d6-c9a1-4f13-89ef-af5b7f7ebd14', 0, 1,
       'd87402c2-8a49-4f68-9c1a-98de6e4ac45d', 0, '', '', 0, '', '',
       null, 0, '', '', 0, '', '',
       null, 0, '', '', 0, '', '',
       'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_SERVICE_RELATION
(SERVICE_RELATION_ID, SOURCE_SERVICE_ID, "ORDER", RELATION_TYPE_CODE,
 TARGET_SERVICE_ID, TARGET_SERVICE_TRANSFORMER_REQUEST_TYPE_CODE, TARGET_SERVICE_TRANSFORMER_REQUEST_METADATA, TARGET_SERVICE_TRANSFORMER_REQUEST_CLASS_NAME, TARGET_SERVICE_TRANSFORMER_RESPONSE_TYPE_CODE, TARGET_SERVICE_TRANSFORMER_RESPONSE_METADATA, TARGET_SERVICE_TRANSFORMER_RESPONSE_CLASS_NAME,
 TARGET_SERVICE_COMMIT_ID, TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_TYPE_CODE, TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_METADATA, TARGET_SERVICE_COMMIT_TRANSFORMER_REQUEST_CLASS_NAME, TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_TYPE_CODE, TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_METADATA, TARGET_SERVICE_COMMIT_TRANSFORMER_RESPONSE_CLASS_NAME,
 TARGET_SERVICE_REVERSE_ID, TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_TYPE_CODE, TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_METADATA, TARGET_SERVICE_REVERSE_TRANSFORMER_REQUEST_CLASS_NAME, TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_TYPE_CODE, TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_METADATA, TARGET_SERVICE_REVERSE_TRANSFORMER_RESPONSE_CLASS_NAME,
 CREATOR, LAST_EDITOR)
VALUES('fc73df1a-9c67-4c8c-9d84-5a765ca94210', '2b6216d6-c9a1-4f13-89ef-af5b7f7ebd14', 0, 1,
       '864d62c0-725c-4da4-91e6-8627fc0971c8', 0, '', '', 0, '', '',
       null, 0, '', '', 0, '', '',
       null, 0, '', '', 0, '', '',
       'Reza Jamshidi', 'Reza Jamshidi');



CREATE TABLE REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (
                                                     TERMINAL_SERVICE_ACCESS_ID VARCHAR(36) NOT NULL,
                                                     TERMINAL_ID VARCHAR(36) NOT NULL,
                                                     SERVICE_ID VARCHAR(36) NOT NULL,
                                                     CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     CREATOR VARCHAR(255),
                                                     LAST_EDITOR VARCHAR(255),
                                                     CONSTRAINT CNST_UNIQUE_TERMINAL_SERVICE UNIQUE (TERMINAL_ID, SERVICE_ID),
                                                     FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
                                                     FOREIGN KEY (SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                                     PRIMARY KEY (TERMINAL_SERVICE_ACCESS_ID)
)
insert into TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_SERVICE_ACCESS_ID, TERMINAL_ID, SERVICE_ID,
                                             CREATOR, LAST_EDITOR)
values ('2a863f7d-2175-4812-93f9-9520be6792a0', 'b9a79451-2141-40b6-98a0-72055a0042c5', '790f7095-0102-44a7-9802-8d2530f09033',
        'Reza Jamshidi', 'Reza Jamshidi');

CREATE TABLE REF.TBL_SCM_ERROR_MAPPING (
                                           ERROR_MAPPING_ID VARCHAR(36) NOT NULL,
                                           EXTERNAL_SERVICE_PROVIDER_ID VARCHAR(36),
                                           PROVIDER_ERROR_CODE VARCHAR(36),
                                           EXCEPTION_CLASS_NAME VARCHAR(300),
                                           SCM_ERROR_CODE VARCHAR(50) NOT NULL,
                                           STATUS_CODE VARCHAR(50) NOT NULL,
                                           MESSAGE VARCHAR(200) NOT NULL,
                                           CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           CREATOR VARCHAR(255),
                                           LAST_EDITOR VARCHAR(255),
                                           FOREIGN KEY (EXTERNAL_SERVICE_PROVIDER_ID) REFERENCES REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER (EXTERNAL_SERVICE_PROVIDER_ID),
                                           PRIMARY KEY (ERROR_MAPPING_ID)
)
INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(ERROR_MAPPING_ID, EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, MESSAGE, CREATOR, LAST_EDITOR)
VALUES('0d8e22e3-79f1-4b02-b7b5-7480f446c723', '', 'SCM-2001', 'SC_ERROR_BUSINESS', '', 'Reza Jamshidi', 'Reza Jamshidi');

CREATE TABLE REF.TBL_SCM_AUTHORITY (
                                       AUTHORITY_ID VARCHAR(36) NOT NULL,
                                       AUTHORITY_TYPE_CODE SMALLINT NOT NULL,
                                       SOURCE_TERMINAL_ID VARCHAR(36) NOT NULL,
                                       SOURCE_CHANNEL_ID VARCHAR(36),
                                       SOURCE_SERVICE_ID VARCHAR(36),
                                       SOURCE_AUTHENTICATION_METHOD_CODE VARCHAR(36),
                                       SOURCE_CONDITION VARCHAR(500),
                                       SOURCE_USER_ID VARCHAR(36),
                                       SOURCE_MEMBERSHIP_ID VARCHAR(36),
                                       TARGET_SERVICE_ACCESS_ALLOW SMALLINT DEFAULT 1,
                                       TARGET_WITHDRAW_DURATION_TYPE_CODE SMALLINT,
                                       TARGET_WITHDRAW_DURATION SMALLINT,
                                       TARGET_WITHDRAW_MIN_AMOUNT DECIMAL(20, 2),
                                       TARGET_WITHDRAW_MAX_AMOUNT DECIMAL(20, 2),
                                       CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       CREATOR VARCHAR(255),
                                       LAST_EDITOR VARCHAR(255),
                                       FOREIGN KEY (SOURCE_TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
                                       FOREIGN KEY (SOURCE_CHANNEL_ID) REFERENCES REF.TBL_SCM_CHANNEL (CHANNEL_ID),
                                       FOREIGN KEY (SOURCE_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                       PRIMARY KEY (AUTHORITY_ID)
)
INSERT INTO "REF".TBL_SCM_AUTHORITY
(AUTHORITY_ID, AUTHORITY_TYPE_CODE, SOURCE_TERMINAL_ID, SOURCE_CHANNEL_ID, SOURCE_SERVICE_ID, SOURCE_AUTHENTICATION_METHOD_CODE, SOURCE_CONDITION, SOURCE_USER_ID, SOURCE_MEMBERSHIP_ID, TARGET_WITHDRAW_DURATION_TYPE_CODE, TARGET_WITHDRAW_DURATION, TARGET_WITHDRAW_MIN_AMOUNT, TARGET_WITHDRAW_MAX_AMOUNT, CREATOR, LAST_EDITOR)
VALUES('8d230ef3-4a9c-4a3a-a8e4-0d8e0a8712c7', 2, 'a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', null, null, null, null, null, null, 1, 1, 0, 1000000000, 'Reza Jamshidi', 'Reza Jamshidi');


CREATE TABLE REF.TBL_SCM_TRANSFORMER (
                                         TRANSFORMER_ID VARCHAR(36) NOT NULL,
                                         TITLE VARCHAR(255),
                                         METADATA VARCHAR(1000),
                                         JAVA_CLASS_NAME VARCHAR(255),
                                         CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         CREATOR VARCHAR(255),
                                         LAST_EDITOR VARCHAR(255),
                                         PRIMARY KEY (TRANSFORMER_ID)
)
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('8f71b0f1-4b53-4c81-a40d-88e6cc53a3a7', 'ایجاد بدنه خالی', 'bean:emptyTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('d6b1a874-2c7c-4932-a6fc-9a7e1d038b03', 'پویا', 'bean:dynamicTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('675e0d37-af60-4f0c-88e7-5a499b20f3ac', 'ریکوئست استعلام شبا', 'ir.daneshrefah.scm.plugin.nab.transformer.IbanInqRequestTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('1f8e84e3-56a3-47f2-ba85-c2a285430824', 'ریسپانس استعلام شبا', 'bean:ibanInqResponseTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('c7943a8d-0db1-4851-951e-1d4a89439f5f', 'افزودن ویژگی', 'bean:appendPropTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('b78fe97e-8e60-45c1-a235-3a0c7f0a6ecf', 'Nab Request Transformer', 'bean:nabRequestTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('fd0a7f8f-af2d-4c5b-aa0f-55e9f58b41ab', 'Nab Response Transformer', 'bean:nabResponseTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
(TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES('a0e4b0c6-9c8a-46c9-aaec-38d3d486b3c3', 'Append LoggedIn User Info', 'bean:appendLoggedInInfo', 'Reza Jamshidi', 'Reza Jamshidi');




CREATE TABLE REF.TBL_SCM_TRANSFORMER_RELATION (
                                                  TRANSFORMER_RELATION_ID VARCHAR(36) NOT NULL,
                                                  RELATION_TYPE_CODE INTEGER,
                                                  ORDER SMALLINT,
                                                  METADATA VARCHAR(1000),
                                                  TRANSFORMER_ID VARCHAR(36),
                                                  SOURCE_ID VARCHAR(36),
                                                  CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                  CREATOR VARCHAR(255),
                                                  LAST_EDITOR VARCHAR(255),
                                                  FOREIGN KEY (TRANSFORMER_ID) REFERENCES REF.TBL_SCM_TRANSFORMER (TRANSFORMER_ID),
                                                  PRIMARY KEY (TRANSFORMER_RELATION_ID)
);
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('f28c1bf7-61ac-4d64-b65a-f7f154e394fc', 1, 1, '675e0d37-af60-4f0c-88e7-5a499b20f3ac', '864d62c0-725c-4da4-91e6-8627fc0971c8', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('aeb6f4e9-c188-4e42-8d8d-3d2f0b59e3e0', 2, 1, '1f8e84e3-56a3-47f2-ba85-c2a285430824', '864d62c0-725c-4da4-91e6-8627fc0971c8', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('5b8a22cc-6a7e-4e4c-9b62-9d3b8d011a35', 3, 1, '8f71b0f1-4b53-4c81-a40d-88e6cc53a3a7', '7d2e1a3b-e894-4ea3-b01a-685361fd75e5', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('dbf21d25-9ff7-4e60-82b1-78e0d428db4a', 4, 1, '[{"sourceProp": "body", "targetProp": "iban"}]', 'c7943a8d-0db1-4851-951e-1d4a89439f5f', '7d2e1a3b-e894-4ea3-b01a-685361fd75e5', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('3d87d9a7-d486-4a20-ae6a-26e9a9a49813', 1, 1, null, 'b78fe97e-8e60-45c1-a235-3a0c7f0a6ecf', 'ebb0b663-8ee2-4b22-9f1a-3d9f985d6a41', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('e1b2465e-263f-4866-84c9-7c37d9f4f487', 2, 1, null, 'fd0a7f8f-af2d-4c5b-aa0f-55e9f58b41ab', 'ebb0b663-8ee2-4b22-9f1a-3d9f985d6a41', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(TRANSFORMER_RELATION_ID, RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES('8927c06b-8304-4ef6-9bf4-64c5d244d3e1', 11, 1, null, 'a0e4b0c6-9c8a-46c9-aaec-38d3d486b3c3', '6d55a24b-63ef-4e25-b376-9d43f4e155cc', 'Reza Jamshidi', 'Reza Jamshidi');


-- 14020424
CREATE TABLE REF.TBL_SCM_PROFILE (
                                     PROFILE_ID VARCHAR(36) NOT NULL,
                                     SERVICE_COMPONENT_PROVIDER_ID VARCHAR(36) NOT NULL,
                                     CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     CREATOR VARCHAR(255),
                                     LAST_EDITOR VARCHAR(255),
                                     NAME VARCHAR(255),
                                     TITLE VARCHAR(255),
                                     PRIMARY KEY (PROFILE_ID)
)
INSERT INTO REF.TBL_SCM_PROFILE (PROFILE_ID, CREATOR, LAST_EDITOR, NAME, TITLE)
VALUES ('a9dcfd02e4fc4f7f92bce0c6a2e7f225', 'Reza Jamshidi', 'Reza Jamshidi', 'IB4DEV', 'اینترنت بانک توسعه');
INSERT INTO REF.TBL_SCM_PROFILE (PROFILE_ID, CREATOR, LAST_EDITOR, NAME, TITLE)
VALUES ('f3e0ebe042f244b09be0581ac05351d0', 'Reza Jamshidi', 'Reza Jamshidi', 'MB4DEV', 'موبایل بانک توسعه');

--------------------------------------------------------------------------------
--Added By Mehdi----------------------------------------------------------------
--------------------------------------------------------------------------------
--Definition of max/limitation for amount and rate
CREATE TABLE REF.TBL_SCM_CONDITION
(
    CONDITION_ID   DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY primary key,
    TITLE          VARCHAR(255) NOT NULL,
    DESC           VARCHAR(255),
    TYPE           SMALLINT NOT NULL,
    VALUE          VARCHAR(255) NOT NULL,
    PERIOD_TYPE    SMALLINT,
    PERIOD_VALUE   SMALLINT,
    CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR VARCHAR(255),
    LAST_EDITOR VARCHAR(255)
);
LABEL ON COLUMN CORPDATA.REF.TBL_SCM_CONDITION.TYPE IS '1: MaxAmount, 2: CallRate, 3: Authority';
-- LABEL ON COLUMN CORPDATA.REF.TBL_SCM_CONDITION.TYPE IS 'Reports to Dept.';
--/////////////////////////////////////////////////////////
--Conditioning terminal per auth and sec.auth method
CREATE TABLE REF.TBL_SCM_TERMINAL_CONDITION(
    TERMINAL_CONDITION_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    TERMINAL_ID VARCHAR(36) NOT NULL,
    STATUS SMALLINT NOT NULL,
    LOGIN_AUTHENTICATION_METHOD_ID SMALLINT,
    TRANSACTION_AUTHENTICATION_METHOD_ID SMALLINT,
    CONDITION_ID DECIMAL(22) NOT NULL,
    CREATE_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CREATOR VARCHAR(255),
    LAST_EDITOR VARCHAR(255),
    FOREIGN KEY (TERMINAL_ID) REFERENCES "REF".TBL_SCM_TERMINAL(TERMINAL_ID),
    FOREIGN KEY (LOGIN_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD(AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (TRANSACTION_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD(AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (CONDITION_ID) REFERENCES "REF".TBL_SCM_CONDITION(CONDITION_ID),
    PRIMARY KEY (TERMINAL_CONDITION_ID)
);
--/////////////////////////////////////////////////////////
--Conditioning service per auth and sec.auth method
CREATE TABLE REF.TBL_SCM_SERVICE_CONDITION(
    SERVICE_CONDITION_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    SERVICE_ID VARCHAR(36) NOT NULL,
    STATUS SMALLINT NOT NULL,
    LOGIN_AUTHENTICATION_METHOD_ID SMALLINT,
    TRANSACTION_AUTHENTICATION_METHOD_ID SMALLINT,
    CONDITION_ID DECIMAL(22) NOT NULL,
    CREATE_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CREATOR VARCHAR(255),
    LAST_EDITOR VARCHAR(255),
    FOREIGN KEY (SERVICE_ID) REFERENCES "REF".TBL_SCM_SERVICE(SERVICE_ID),
    FOREIGN KEY (LOGIN_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD(AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (TRANSACTION_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD(AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (CONDITION_ID) REFERENCES "REF".TBL_SCM_CONDITION(CONDITION_ID),
    PRIMARY KEY (SERVICE_CONDITION_ID)
);
--/////////////////////////////////////////////////////////
--Conditioning the combination of terminal and service, per auth and sec.auth method
CREATE TABLE REF.TBL_SCM_TERMINAL_SERVICE_CONDITION(
    TERMINAL_SERVICE_CONDITION_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    TERMINAL_SERVICE_ACCESS_ID VARCHAR(36) NOT NULL,
    STATUS SMALLINT NOT NULL,
    LOGIN_AUTHENTICATION_METHOD_ID SMALLINT,
    TRANSACTION_AUTHENTICATION_METHOD_ID SMALLINT,
    CONDITION_ID DECIMAL(22) NOT NULL,
    CREATE_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CREATOR VARCHAR(255),
    LAST_EDITOR VARCHAR(255),
    FOREIGN KEY (TERMINAL_SERVICE_ACCESS_ID) REFERENCES "REF".TBL_SCM_TERMINAL_SERVICE_ACCESS(TERMINAL_SERVICE_ACCESS_ID),
    FOREIGN KEY (LOGIN_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD(AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (TRANSACTION_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD(AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (CONDITION_ID) REFERENCES "REF".TBL_SCM_CONDITION(CONDITION_ID),
    PRIMARY KEY (TERMINAL_SERVICE_CONDITION_ID)
);
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_UAA_USER_GROUP (
    --Definition
                                        USER_GROUP_ID  VARCHAR(36) NOT NULL,
                                        USER_GROUP_CODE  VARCHAR(36) NOT NULL,
    --Attribute
                                        STATUS         SMALLINT,
                                        TITLE          VARCHAR(255),
    --Versioning
                                        CREATE_DATE    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        CREATOR        VARCHAR(255),
                                        LAST_EDITOR    VARCHAR(255),
    --Relation
                                        PRIMARY KEY (USER_GROUP_ID)
);--Done
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET (
    --Definition
                                   ASSET_ID       VARCHAR(36) NOT NULL, --TODO AutoIncrement
                                   PROVIDER_ID    VARCHAR(255) NOT NULL,
                                   ASSET_TYPE_CODE  SMALLINT NOT NULL,
    --Attribute
    --Versioning
                                   CREATE_DATE    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CREATOR        VARCHAR(255),
                                   LAST_EDITOR    VARCHAR(255),
    --Relation
                                   FOREIGN KEY (PROVIDER_ID) REFERENCES REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER (EXTERNAL_SERVICE_PROVIDER_ID),
                                   PRIMARY KEY (ASSET_ID)
);--Done
--/////////////////////////////////////////////////////////
-- REF.CUSTOMER--Done
ALTER TABLE REF.CUSTOMER ADD COLUMN PROVIDER_ID VARCHAR(36) NOT NULL DEFAULT '3ce3e10e-c3cd-49c7-ae5c-330a81e882d7';
ALTER TABLE REF.CUSTOMER ADD FOREIGN KEY (PROVIDER_ID) REFERENCES REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER(EXTERNAL_SERVICE_PROVIDER_ID); -- todo rename TBL_SCM_EXTERNAL_SERVICE_PROVIDER

--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_CUSTOMER_ASSET( --todo use membership & customerAccount & card tables
    --Definition
                                           CUSTOMER_ASSET_ID VARCHAR(36)  NOT NULL,
                                           CUSTOMER_ID       INTEGER      NOT NULL,
                                           ASSET_ID          VARCHAR(255) NOT NULL,
                                           ASSET_ACCOUNT_REF_ID      VARCHAR(255),--reference ID to asset's table, related to asset type (ASSET_TYPE_ID). ACC type for account, CRD type for card, ...
                                           ASSET_CARD__REF_ID      VARCHAR(255),--todo define foreign key for both account & card

    --Attribute
                                           RELATION_TYPE     SMALLINT     NOT NULL,--0, Owner, 1: Advocacy, 2: Delegation, 4:...
    --Versioning
                                           CREATE_DATE       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           LAST_EDIT_DATE    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           CREATOR           VARCHAR(255),
                                           LAST_EDITOR       VARCHAR(255),
    --Relation
                                           FOREIGN KEY (CUSTOMER_ID) REFERENCES REF.CUSTOMER (CUSTOMER_ID),
                                           FOREIGN KEY (ASSET_ID) REFERENCES REF.TBL_SCM_ASSET (ASSET_ID),
                                           PRIMARY KEY (CUSTOMER_ASSET_ID)
);
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET_ACCOUNT ( -- todo use account table
    --Definition
                                           ASSET_ACCOUNT_ID VARCHAR(36) NOT NULL,
    --Attribute
                                           ACCOUNT_NO       VARCHAR(36) NOT NULL,
                                           SHEBA_NO         VARCHAR(36) NOT NULL,
                                           STATUS           SMALLINT NOT NULL,--0: Block, 1: Active, 3: Suspend, 4: Closed
                                           PRODUCT_TYPE     SMALLINT,--Should define the product type
                                           ACCOUNT_TYPE     SMALLINT,--Should define the sup type of product --todo define foreignKey to AccountType
                                           TITLE            VARCHAR(36) NOT NULL,
                                           TYPE             SMALLINT NOT NULL,--0: Individual, 1: Legal
                                           SHARED           BOOLEAN NOT NULL,
    --
                                           TERM             SMALLINT,--1: all, 2: account, 3: loan,...
                                           RATE             SMALLINT,
                                           DUE_DATE         TIMESTAMP,
                                           INTEREST_ACCOUNT VARCHAR(36) NOT NULL,
                                           DUE_DATE_ACCOUNT VARCHAR(36) NOT NULL,

    --Versioning
                                           CREATE_DATE      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           LAST_EDIT_DATE   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                           CREATOR          VARCHAR(255),
                                           LAST_EDITOR      VARCHAR(255),
    --Relation
                                           PRIMARY KEY (ASSET_ACCOUNT_ID)
);--Done
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET_CARD ( --todo use card table
    --Definition
                                        ASSET_CARD_ID  VARCHAR(36) NOT NULL,

    --Attribute
                                        CARD_NO        VARCHAR(36) NOT NULL,
                                        STATUS         SMALLINT NOT NULL,--0: Block, 1: Active, 3: Suspend, 4: Closed
                                        PRODUCT_TYPE   SMALLINT,--Should define the product type
                                        CVV2           SMALLINT NOT NULL,--Should define the sup type of product
                                        EXPIRE_DATE    TIMESTAMP NOT NULL,
                                        MEDIA_TYPE     SMALLINT,--0: Magnet, 1: Smart, 2: eCard
                                        HOLDER         VARCHAR(36) NOT NULL,
    --Versioning
                                        CREATE_DATE    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        CREATOR        VARCHAR(255),
                                        LAST_EDITOR    VARCHAR(255),
    --Relation
                                        PRIMARY KEY (ASSET_CARD_ID)
);--Done
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET_CARD_ACCOUNT ( --todo use card table
    --Definition
                                                ASSET_CARD_ACCOUNT_ID VARCHAR(36) NOT NULL,
                                                ASSET_CARD_ID         VARCHAR(36) NOT NULL,
                                                ASSET_ACCOUNT_ID      VARCHAR(36) NOT NULL,
    --Attribute
                                                MAIN_ACCOUNT          BOOLEAN NOT NULL,
                                                INDEX                 SMALLINT,
    --Versioning
                                                CREATE_DATE           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                LAST_EDIT_DATE        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                CREATOR               VARCHAR(255),
                                                LAST_EDITOR           VARCHAR(255),
    --Relation
                                                FOREIGN KEY (ASSET_CARD_ID) REFERENCES REF.TBL_SCM_ASSET_CARD (ASSET_CARD_ID),
                                                FOREIGN KEY (ASSET_ACCOUNT_ID) REFERENCES REF.TBL_SCM_ASSET_ACCOUNT (ASSET_ACCOUNT_ID),
                                                PRIMARY KEY (ASSET_CARD_ACCOUNT_ID)
);--Done
--/////////////////////////////////////////////////////////
--Alter
--REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER--Done
ALTER TABLE REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER ADD COLUMN CUSTOMER_PROVIDE_METHOD_CODE SMALLINT NOT NULL;
ALTER TABLE REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER ADD COLUMN STATUS   SMALLINT;

--/////////////////////////////////////////////////////////

CREATE TABLE REF.TBL_SCM_PERSON_SERVICE_ACCESS (
                                                   PERSON_SERVICE_ACCESS_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
                                                   PERSON_PROFILE_ID VARCHAR(10) NOT NULL,
                                                   SERVICE_ID VARCHAR(36) NOT NULL,
                                                   TERMINAL_ID VARCHAR(36),
                                                   ASSET_ID VARCHAR(100),
                                                   CREATE_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                   LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                   CREATOR VARCHAR(255),
                                                   LAST_EDITOR VARCHAR(255),
                                                   FOREIGN KEY (SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
                                                   FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
--     FOREIGN KEY (PERSON_PROFILE_ID) REFERENCES REF.USER (USERNAME),
                                                   PRIMARY KEY (PERSON_SERVICE_ACCESS_ID)
);
