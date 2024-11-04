CREATE TABLE REF.TBL_SCM_TERMINAL
(
    --Definition
    TERMINAL_ID                         VARCHAR(36) NOT NULL,
    CODE                                VARCHAR(255),
    TITLE                               VARCHAR(255),
    LEGACY_TERMINAL_ID                  INTEGER     NOT NULL,
    --Attribute
    STATUS                              SMALLINT    NOT NULL DEFAULT 1,
    SUPPORT_CHECK_AUTHENTICATION        SMALLINT    NOT NULL DEFAULT 1,
    SUPPORT_CHECK_SECOND_AUTHENTICATION SMALLINT    NOT NULL DEFAULT 1,
    SUPPORT_CHECK_SERVICE_ACCESS        SMALLINT    NOT NULL DEFAULT 1,
    SUPPORT_CHECK_ASSET_ACCESS          SMALLINT    NOT NULL DEFAULT 1,
    SUPPORT_CUSTOMER_INJECTION          SMALLINT    NOT NULL DEFAULT 1,
--     SUPPORT_CUSTOMER_INJECTION          SMALLINT             DEFAULT 1,
    --Versioning
    CREATE_DATE                         TIMESTAMP            DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE                      TIMESTAMP            DEFAULT CURRENT TIMESTAMP,
    CREATOR                             VARCHAR(255),
    LAST_EDITOR                         VARCHAR(255),
    --Relation
    FOREIGN KEY (LEGACY_TERMINAL_ID) REFERENCES REF.CHANNEL (CHANNEL_ID),
    PRIMARY KEY (TERMINAL_ID)
);
INSERT INTO REF.TBL_SCM_TERMINAL (TERMINAL_ID, CODE, TITLE, LEGACY_TERMINAL_ID, CREATOR, LAST_EDITOR)
VALUES ('3f0b9c5a-8d89-4c8d-9a7d-4287f6e75639', 'MB', 'موبایل بانک', 211, 'Reza Jamshidi', 'Reza Jamshidi');

INSERT INTO REF.TBL_SCM_TERMINAL (TERMINAL_ID, CODE, TITLE, LEGACY_TERMINAL_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', 'IB', 'اینترنت بانک', 210, 'Reza Jamshidi', 'Reza Jamshidi');

INSERT INTO REF.TBL_SCM_TERMINAL (TERMINAL_ID, CODE, TITLE, LEGACY_TERMINAL_ID, SUPPORT_CHECK_SERVICE_ACCESS,
                                  SUPPORT_CHECK_ASSET_ACCESS, SUPPORT_CUSTOMER_INJECTION, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'SCM', 'مدیریت کانال', 22, 0, 0, 0, 'Reza Jamshidi', 'Reza Jamshidi');



CREATE TABLE REF.TBL_SCM_CHANNEL
(
    CHANNEL_ID         VARCHAR(36) NOT NULL,
    CODE               VARCHAR(255),
    TITLE              VARCHAR(255),
    TERMINAL_ID        VARCHAR(36) NOT NULL,
    PROTOCOL           SMALLINT    NOT NULL,
    CHANNEL_CLASS_NAME VARCHAR(255),
    METADATA           VARCHAR(1000),
    CREATE_DATE        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR            VARCHAR(255),
    LAST_EDITOR        VARCHAR(255),
    FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
    PRIMARY KEY (CHANNEL_ID)
);

INSERT INTO TBL_SCM_CHANNEL (CHANNEL_ID, CODE, TITLE, TERMINAL_ID, PROTOCOL, METADATA, CREATOR, LAST_EDITOR)
VALUES ('a4d7637c-0855-4e81-a157-70b02742de24', 'SCM4DEV', 'محیط توسعه مدیریت کانال',
        'b9a79451-2141-40b6-98a0-72055a0042c5', 2, '{"contextPath": "/scm4dev", "port": 8083}', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO TBL_SCM_CHANNEL (CHANNEL_ID, CODE, TITLE, TERMINAL_ID, PROTOCOL, METADATA, CREATOR, LAST_EDITOR)
VALUES ('fc32467b-47cb-45c4-9076-2f7cc7d40f75', 'IB4DEV', 'محیط توسعه اینترنت بانک',
        'a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', 2, '{"contextPath": "/ib4dev", "port": 8082}', 'Reza Jamshidi',
        'Reza Jamshidi');

--

CREATE TABLE REF.TBL_SCM_SERVICE_PROVIDER
(
    SERVICE_PROVIDER_ID    VARCHAR(36) NOT NULL,
    CODE                   VARCHAR(255),
    TITLE                  VARCHAR(255),
    STATUS                 SMALLINT  DEFAULT NULL,
    PROTOCOL               SMALLINT    NOT NULL,
    PROVIDER_CLASS_NAME    VARCHAR(255),
    METADATA               VARCHAR(255),
--     CUSTOMER_PROVIDED   SMALLINT    NOT NULL DEFAULT 0,
--     CUSTOMER_PROVIDER_CLASS_NAME VARCHAR(255),
    CORE_BANKING_SYSTEM_ID SMALLINT  ,
    CREATE_DATE            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                VARCHAR(255),
    LAST_EDITOR            VARCHAR(255),
    PRIMARY KEY (SERVICE_PROVIDER_ID),
    FOREIGN KEY (CORE_BANKING_SYSTEM_ID) REFERENCES REF.CORE_BANKING_SYSTEM (CORE_BANKING_SYSTEM_ID)
);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                          CORE_BANKING_SYSTEM_ID, CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('3ce3e10e-c3cd-49c7-ae5c-330a81e882d7', 'NAB', 'کر بانک رفاه', 1, 'bean:nabCoreServiceProvider',
        '{"endpoint" : "${scm.provider.nab}"}', 1, 'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROTOCOL, PROVIDER_CLASS_NAME,
                                          METADATA, CORE_BANKING_SYSTEM_ID, CREATOR, LAST_EDITOR)
VALUES ('D487B21A-CA18-438E-A9E9-17482B127C81', 'NAB-TCP', 'کر بانک رفاه', 1, 5, 'bean:nabTcpServiceProvider',
        '{"endpoint" : "${scm.provider.nab-tcp}"}', 1, 'Reza Jamshidi', 'Reza Jamshidi');

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA
                                    , CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('ce027926-e5e1-4df5-b397-178dd41c87b8', 'MOCK', 'Mock', 1, 'bean:mockCoreServiceProvider', '',
        'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                         CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('710fe18f-41cc-40f1-8435-3d4626289b3c', 'SCM', 'SCM', 1, 'bean:scmCoreServiceProvider', null,
        'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                            CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('3A3C6EBC-328C-484A-A6F4-43E734F2312C', 'IBAN', 'IBAN', 1, 'bean:ibanInquiryServiceProvider',
        '{"endpoint" : "${scm.provider.iban}"}',   'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('E81F2B7D-7B1A-4D16-A8A9-4F1212F23ABC', 'HPS', 'HPS', 1, 'bean:hpsServiceProvider', null,
        'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                            CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('1F22212B-D23E-442B-92F2-0B12312E4F56', 'CHAKAD', 'CHAKAD', 1, 'bean:chakadServiceProvider',
        '{"endpoint" : "${scm.provider.chakad}"}',   'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 'PICHACK', 'PICHACK', 1, 'bean:pichackServiceProvider',
        '{"endpoint" : "${scm.provider.pichack}"}',   'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('5C92871D-212E-4F2A-8923-123F231A2BEC', 'SAYAD', 'SAYAD', 1, 'bean:sayadServiceProvider',
        '{"endpoint" : "${scm.provider.sayad}"}',   'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('B21A423D-F21B-4522-812F-2312BEFA2C1D', 'BILL-INQUIRY', 'BILL-INQUIRY', 1, 'bean:billInquiryServiceProvider',
        '{"endpoint" : "${scm.provider.bill-inquiry}"}', 'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('0D8F312E-1B2A-431F-A212-23F2312DECBA', 'CURRENCY', 'سامانه ارزی', 1, 'bean:currencyServiceProvider',
        '{"endpoint" : "${scm.provider.currency}"}', 'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('4EF21A2C-B13D-412B-B2FA-23A12F231BEC', 'GSS', 'GSS', 1, 'bean:gssServiceProvider',
        '{"endpoint" : "${scm.provider.gss}"}', 'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('7921CFEB-A32D-4BFA-A12E-321F2312A1BC', 'LOAN', 'LOAN', 1, 'bean:loanServiceProvider',
        '{"endpoint" : "${scm.provider.loan}"}', 'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('F1AB23CD-21FA-421D-B1AF-312F231BEABC', 'TOPUP', 'TOPUP', 1, 'bean:topupServiceProvider',
        '{"endpoint" : "${scm.provider.topup}"}',  'Reza Jamshidi', 'Reza Jamshidi',5);

INSERT INTO REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID, CODE, TITLE, STATUS, PROVIDER_CLASS_NAME, METADATA,
                                           CREATOR, LAST_EDITOR,PROTOCOL)
VALUES ('6D4A2C1E-B21D-423B-AF21-C234F21A1BEC', 'SHAPARAK', 'هاب فناوران (شاپرک)', 1, 'bean:shaparakServiceProvider',
        '{"endpoint" : "${scm.provider.shaparak}"}',
        'Reza Jamshidi', 'Reza Jamshidi',5);

CREATE TABLE REF.TBL_SCM_SERVICE
(
    --Definition
    SERVICE_ID                           VARCHAR(36)             NOT NULL,
    CODE                                 VARCHAR(255)            NOT NULL,
    TITLE                                VARCHAR(255),
    "ALIAS"                              VARCHAR(255),
    VERSION                              SMALLINT      DEFAULT 1,
    --Attribute
    STATUS                               SMALLINT,
    IS_SYSTEMIC                          SMALLINT      DEFAULT 0,
    REQUEST_JSON_SCHEMA                  VARCHAR(3000),
    RESPONSE_JSON_SCHEMA                 VARCHAR(2500),
    PARENT_SERVICE_ID                    VARCHAR(36),
    SERVICE_TYPE_CODE                    INTEGER,
    SERVICE_IMPLEMENTATION_TYPE_CODE     INTEGER                 NOT NULL,
    IMPLEMENTATION_JAVA_CLASS_NAME       VARCHAR(255),
    IMPLEMENTATION_SERVICE_PROVIDER_ID   VARCHAR(36),
    IMPLEMENTATION_BPMN_CONTENT          VARCHAR(1000),
    IMPLEMENTATION_COMPOSITION_TYPE_CODE INTEGER,
    CHECK_ACCESS_FIRST_AUTHENTICATION    SMALLINT      DEFAULT 0 NOT NULL,
    CHECK_ACCESS_SECOND_AUTHENTICATION   SMALLINT      DEFAULT 0 NOT NULL,
    CHECK_ACCESS_SERVICE                 SMALLINT      DEFAULT 0 NOT NULL,
    CHECK_ACCESS_ASSET                   SMALLINT      DEFAULT 0 NOT NULL,
--     IS_CUSTOMER_BASED                    SMALLINT      DEFAULT 0 NOT NULL,
    PROPERTY_NAME_AMOUNT                 VARCHAR(100),
    PROPERTY_NAME_ASSET                  VARCHAR(100),
    PROPERTY_NAME_CUSTOMER               VARCHAR(100),
    METADATA                             varchar(5500) DEFAULT NULL,
    --Versioning
    CREATE_DATE                          TIMESTAMP     DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE                       TIMESTAMP     DEFAULT CURRENT TIMESTAMP,
    CREATOR                              VARCHAR(255),
    LAST_EDITOR                          VARCHAR(255),
    --External service
    REQUEST_BODY_TYPE                    VARCHAR(64),
    PATH                                 VARCHAR(1024),
    HTTP_METHOD                          VARCHAR(16),
    REQUEST_CONTENT_TYPE                 VARCHAR(128),
    --Relation
    CONSTRAINT CNST_UNIQUE_CODE UNIQUE (CODE),
    FOREIGN KEY (PARENT_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    FOREIGN KEY (IMPLEMENTATION_SERVICE_PROVIDER_ID) REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID),
    PRIMARY KEY (SERVICE_ID)
);
--------------------------------------------- TERMINAL_MANAGEMENT_SERVICES ---------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('23a5aaff-a92e-4896-a873-39e4ff75a419', 'SVC_TERMINAL_PARENT', 'پرنت ترمینال', '/terminal', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('bd0fe44f-68f2-4952-8749-fb15b05f0760', 'SVC_TERMINAL_LIST', 'لیست ترمینال', '/list', 1, 1, 1, 2, 1,
        'bean:terminalManagementService.listTerminal(ir.daneshrefah.scm.common.dto.terminal.TerminalFindRequest)',
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('E1FA2B3D-212A-421B-A23C-12FA3C1BEDA', 'SVC_TERMINAL_FIND_BY_ID', 'بازیابی اطلاعات ترمینال با شناسه',
        '/{terminalId}', 1, 1, 3, 2, 1,
        'bean:terminalManagementService.findTerminalById(String)', '23a5aaff-a92e-4896-a873-39e4ff75a419',
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('d42c6df9-e27d-4894-b86c-a48dd2a11412', 'SVC_TERMINAL_CREATE', 'ایجاد ترمینال', '', 1, 1, 5, 2, 1,
        'bean:terminalManagementService.createTerminal(ir.daneshrefah.scm.common.dto.terminal.TerminalCreateRequest)',
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('3AB12C3E-1CFA-412B-A23C-12FA3C2BEDA', 'SVC_TERMINAL_EDIT', 'ویرایش ترمینال', '', 1, 1, 6, 2, 1,
        'bean:terminalManagementService.editTerminal(ir.daneshrefah.scm.common.dto.terminal.TerminalEditRequest)',
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('2BFA1C2E-A31D-412A-B23F-23EFABC1DEC', 'SVC_TERMINAL_DELETE', 'حذف ترمینال', '', 1, 1, 7, 2, 1,
        'bean:terminalManagementService.deleteTerminal(ir.daneshrefah.scm.common.dto.terminal.TerminalDeleteRequest)',
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('44fddd2a-5df4-4a8a-a4c2-5381ef673e02', 'SVC_TERMINAL_ADD_SERVICE', 'تخصیص سرویس به ترمینال', '/add-service', 1,
        1, 5, 2, 1,
        'bean:terminalManagementService.addServiceAssignment(ir.daneshrefah.scm.common.dto.terminal.TerminalServiceAssignmentRequest)',
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('7D2C1AEF-A23A-4B2E-A1FE-32EF2ABC1DEA', 'SVC_TERMINAL_DELETE_SERVICE', 'عدم تخصیص سرویس به ترمینال',
        '/delete-service', 1, 1, 7, 2, 1,
        'bean:terminalManagementService.deleteServiceAssignment(ir.daneshrefah.scm.common.dto.terminal.TerminalServiceAssignmentRequest)',
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('69d6a8e0-f090-4e41-971f-a03b4a6b388f', 'SVC_TERMINAL_ACCESS_TERMINAL_LIST',
        'دریافت لیست ترمینال های در دسترس سرویس', '/access-terminal-list/{serviceId}', 1, 1, 1, null, null,
        '23a5aaff-a92e-4896-a873-39e4ff75a419', 3, 2,
        'bean:terminalManagementService.findAllTerminalAccessOnService(String)', null, null, null, 1, 0, 0, 0, null,
        null, null, null, '2024-04-22 10:04:42.650215', '2024-04-22 13:35:13.388796', 'Dariush Abdolahi',
        'Dariush Abdolahi');

--------------------------------------------- CHANNEL_MANAGEMENT_SERVICES ---------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('0D2E3FEA-A32C-4B2F-A12F-12EFABC1DEC', 'SVC_CHANNEL_PARENT', 'پرنت کانال', '/channel', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('7B1A2C3E-21FA-421C-B23D-12FA3C1BEDA', 'SVC_CHANNEL_LIST', 'لیست کانال', '/list', 1, 1, 1, 2, 1,
        'bean:channelManagementService.listChannel(ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest)',
        '0D2E3FEA-A32C-4B2F-A12F-12EFABC1DEC', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('5EF13AB1-2B1C-421F-A12E-23EFABC1DEC', 'SVC_CHANNEL_FIND_BY_ID', 'بازیابی اطلاعات کانال با شناسه',
        '/{channelId}', 1, 1, 3, 2, 1,
        'bean:channelManagementService.findChannelById(String)', '0D2E3FEA-A32C-4B2F-A12F-12EFABC1DEC', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('DB1A2C3E-2BFA-421C-B12E-12FA3C1BEDA', 'SVC_CHANNEL_CREATE', 'ایجاد کانال', '', 1, 1, 5, 2, 1,
        'bean:channelManagementService.createChannel(ir.daneshrefah.scm.common.dto.channel.ChannelCreateRequest)',
        '0D2E3FEA-A32C-4B2F-A12F-12EFABC1DEC', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('0C2E3FEA-A12C-432F-A21F-12EFABC1DEC', 'SVC_CHANNEL_EDIT', 'ویرایش کانال', '', 1, 1, 6, 2, 1,
        'bean:channelManagementService.editChannel(ir.daneshrefah.scm.common.dto.channel.ChannelEditRequest)',
        '0D2E3FEA-A32C-4B2F-A12F-12EFABC1DEC', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('AB3F2C3E-A12D-421A-B23B-23FA3C2BEAD', 'SVC_CHANNEL_DELETE', 'حذف کانال', '', 1, 1, 7, 2, 1,
        'bean:channelManagementService.deleteChannel(ir.daneshrefah.scm.common.dto.channel.ChannelDeleteRequest)',
        '0D2E3FEA-A32C-4B2F-A12F-12EFABC1DEC', 'Reza Jamshidi', 'Reza Jamshidi');

--------------------------------------------- SERVICE_MANAGEMENT_SERVICES ----------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('616d1811-0ad6-4f3c-80c0-47a003522d08', 'SVC_SERVICE_PARENT', 'پرنت سرویس', '/service', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('790f7095-0102-44a7-9802-8d2530f09033', 'SVC_SERVICE_LIST', 'لیست سرویس', '/list', 1, 1, 1, 2, 1,
        'bean:serviceManagementService.serviceList(ir.daneshrefah.scm.common.dto.ServiceFindRequest)',
        '616d1811-0ad6-4f3c-80c0-47a003522d08', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('d2220c12-30be-4305-9706-91d699826731', 'SVC_SERVICE_BY_CODE', 'یافتن سرویس با کد', '/code/{serviceCode}', 1, 1,
        3, 2, 1,
        'bean:serviceManagementService.findServiceByCode(String)', '616d1811-0ad6-4f3c-80c0-47a003522d08',
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('ee6380f7-3f2d-4751-8211-74a67370635e', 'SVC_SERVICE_CREATE', 'ایجاد سرویس', '', 1, 1, 5, 2, 1,
        'bean:serviceManagementService.createService(ir.daneshrefah.scm.common.dto.ServiceInfoRequest)',
        '616d1811-0ad6-4f3c-80c0-47a003522d08', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('819da0df-a024-4c3a-93b1-120a79084c27', 'SCV_SERVICE_DELETE', 'حذف سرویس', '', 1, 1, 1, null, null,
        '616d1811-0ad6-4f3c-80c0-47a003522d08', 7, 2,
        'bean:serviceManagementService.deleteService(ir.daneshrefah.scm.common.dto.ServiceDeleteRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-16 13:27:10.000000', '2024-04-16 13:27:12.000000',
        'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('455d9247-e05c-49e6-b504-ad9bf2b9241c', 'SVC_SERVICE_EDIT', 'ویرایش سرویس', '/edit', 1, 1, 1, null, null,
        '616d1811-0ad6-4f3c-80c0-47a003522d08', 6, 2,
        'bean:serviceManagementService.updateService(ir.daneshrefah.scm.common.dto.ServiceInfoEditRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-17 10:29:03.460568', '2024-04-17 10:29:03.460568',
        'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('33f6f9aa-d651-447a-a5e5-8a80d2941f57', 'SVC_SERVICE_ACCESS_SERVICE_LIST',
        'دریافت لیست سرویس های در دسترس ترمینال', '/access-service-list', 1, 1, 1, null, null,
        '616d1811-0ad6-4f3c-80c0-47a003522d08', 3, 5,
        'bean:serviceManagementService.findAllServiceAccessOnTerminal(ir.daneshrefah.scm.common.dto.ServiceAccessFindRequest)',
        null, null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-22 15:10:01.000000',
        '2024-04-22 15:10:03.000000', 'Dariush Abdolahi', 'Dariush Abdolahi');

--------------------------------------------- ACCOUNT_MANAGEMENT_SERVICES ----------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('c0a707a8-7330-43b1-bda2-d6e5bf0ee691', 'SVC_ACCOUNT_PARENT', 'پرنت حساب', '/account', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE,PATH, TITLE, STATUS, METADATA, IS_SYSTEMIC, PARENT_SERVICE_ID,
                                 SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 PROPERTY_NAME_CUSTOMER, CREATOR, LAST_EDITOR)
VALUES ('41088034-ad79-4f6a-9e04-4f6a31374148', 'SVC_NAB_CUSTOMER_ACCOUNT_LIST','SCMREAD.GETCUSTOMERACCOUNTS', 'لیست حساب همراه', 2,
        '{"serviceName":"SCMREAD.GETCUSTOMERACCOUNTS","rq":{"parameters":[{"name":"P_CUSTOMERID","$value":{"length":10,"type":"string","fromValue":"customerNo","convertor":"fixString"}},{"name":"P_SIGNER","value":"-1"},{"name":"P_ACCOUNTSTATUS","value":"-1"},{"name":"P_INCLUDECLOSED","value":"1"},{"name":"P_STARTROW","value":"1"},{"name":"P_ENDROW","value":"20"}],"callType":"Executer","encoding":"ASCII","requestID":"123456789"},"rs":{"$accountOwnerCustomerNo":{"fromValue":"OWNERID"},"$accountOwnerName":{"fromValue":"OWNERNAME"},"$accountOwnerCustomerTypeCode":{"fromValue":"CUSTOMERTYPE"},"$accountOwnerCustomerTypeTitle":{"fromValue":"CUSTOMERTITILE"},"$generalCode":{"fromValue":"GENERAL"},"$subsidryCode":{"fromValue":"SUBSIDRY"},"$subsidryTitle":{"fromValue":"SUBSIDRYTITLE"},"$accountTypeCode":{"fromValue":"ACCOUNTTYPE"},"$accountTypeTitle":{"fromValue":"ACCOUNTTYPETITLE"},"$accountNumber":{"fromValue":"ACCOUNTNUMBER"},"$accountStatusCode":{"fromValue":"ACCOUNTSTATUS"},"$accountStatusTitle":{"fromValue":"ACCOUNTSTATUSTITLE"},"$branchCode":{"fromValue":"BRANCHCODE"},"$branchTitle":{"fromValue":"BRANCHTITILE"},"$accountOpenDate":{"fromValue":"OPENACCOUNTDATE"},"$accountIsCommercial":{"fromValue":"ISCOMMERCE"},"$currency":{"fromValue":"ACCOUNTCURRENCY"},"$iban":{"fromValue":"IBAN"},"$sayahCode":{"fromValue":"SAYAHCODE"},"$customerRelationTypeCode":{"fromValue":"RELATIONTYPECODE"},"$customerRelationTypeTitle":{"fromValue":"RELATIONTYPETITLE"},"$customerIsSigner":{"fromValue":"ISSIGNER"},"$customerSharePercent":{"fromValue":"SHAREPERCENT"},"$balanceTotal":{"fromValue":"BALANCETOTAL"},"$balanceAvailable":{"fromValue":"BALANCEAVAILABLE"},"$blockTotal":{"fromValue":"BLOCKTOTAL"},"$signDate":{"fromValue":"SIGNDATE"},"$lastTransactionDate":{"fromValue":"LASTTRANSDATE"}},"$mainOwner":{"length":10,"type":"string","fromValue":"MAINOWNER","convertor":"latinToPersianConvertor"}}',
        1, 'c0a707a8-7330-43b1-bda2-d6e5bf0ee691', 3, 6, '3ce3e10e-c3cd-49c7-ae5c-330a81e882d7', 'customerNo',
        'Reza Jamshidi', 'Reza Jamshidi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, PARENT_SERVICE_ID,
                                 SERVICE_TYPE_CODE, SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION, CREATOR,
                                 LAST_EDITOR)
VALUES ('45bcf9af-2614-4a2e-933a-3121c3fb2c06', 'SVC_ACCOUNT_WITHDRAW_TABLE', 'جدول شرایط برداشت حساب',
        '/{accountNo}/withdraw', 1, 1, 'c0a707a8-7330-43b1-bda2-d6e5bf0ee691', 3, 3,
        2, 1, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, STATUS, METADATA, IS_SYSTEMIC, PARENT_SERVICE_ID,
                                 SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_ASSET, CREATOR, LAST_EDITOR)
VALUES ('5EF23AB1-2A1C-421F-A12E-23EFABC1DEC', 'SVC_NAB_ACCOUNT_WITHDRAW_TABLE', 'جدول شرایط برداشت همراه', 2,
        '{"serviceName": "SCMREAD.GETACCOUNTWITHDRAWTABLE", "rq": {"parameters": [{"name": "P_ACCOUNTID", "$value": {"type": "string", "fromValue": "accountNo"}}, {"name": "P_EFFECTIVEDATE", "value": "0"}], "callType": "Reader", "encoding": "ASCII", "requestID": "RequestID"}, "rs": {"$signNo": {"fromValue": "SIGNNO"}, "$srlSign": {"fromValue": "SRLSIGN"}, "$customerId": {"fromValue": "CUSTOMERID"}, "$firstName": {"fromValue": "FIRSTNAME"}, "$lastName": {"fromValue": "LASTNAME"}, "$nationalId": {"fromValue": "NATIONALID"}, "$subOrg": {"fromValue": "SUBORG"}, "$startDate": {"fromValue": "STARTDATE"}, "$endDate": {"fromValue": "ENDDATE"}}, "$mainOwner": {"length": 10, "type": "string", "fromValue": "MAINOWNER", "convertor": "latinToPersianConvertor"}}',
        1, 'c0a707a8-7330-43b1-bda2-d6e5bf0ee691', 3, 1, '3ce3e10e-c3cd-49c7-ae5c-330a81e882d7', 1, 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('F12A3BED-A32B-432E-A21F-31EF1ABC2DEA', 'SVC_USER_LOCAL_ACCOUNT_LIST', 'لیست حسابهای یک کاربر', '/local', 1, 1,
        1, 2, 1,
        'bean:customerManagementService.findLocalMembershipTerminalAccesses(ir.daneshrefah.scm.common.dto.membership.MembershipFindRequest)',
        'c0a707a8-7330-43b1-bda2-d6e5bf0ee691', 'Reza Jamshidi', 'Reza Jamshidi');

--------------------------------------------- FUND_TRANSFER_SERVICES ----------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('A321C87B-F28D-402D-87A2-1C34A78219BD', 'SVC_INTERNAL_XFER_PARENT', 'پرنت انتقال وجه داخلی', '/xfer', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');

INSERT INTO REF.TBL_SCM_SERVICE
(SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA, RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID,
 SERVICE_TYPE_CODE, SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
 METADATA, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, PROPERTY_NAME_CUSTOMER, CREATOR, LAST_EDITOR)
VALUES ('DE3A812C-B741-401B-A012-8C4271A2BDEF', 'SVC_INTERNAL_XFER_ADD', 'افزودن انتقال وجه داخلی', '', 1, 1, '{"$schema":"http://json-schema.org/draft-07/schema#","title":"GeneratedschemaforRoot","type":"object","properties":{"sourceAccount":{"type":"string"},"destinationAccount":{"type":"string"},"amount":{"type":"number"},"paymentId":{"type":"string"}},"required":["sourceAccount","destinationAccount","amount"]}',
        null, 'A321C87B-F28D-402D-87A2-1C34A78219BD', 2, 1, 'D487B21A-CA18-438E-A9E9-17482B127C81', 1, 1, 0, 1,
        null, null, null, null, 'Reza Jamshidi', 'Reza Jamshidi');


INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('21EBAC34-182A-471D-80A1-C2B7412A834E', 'SVC_ACH_XFER_PARENT', 'پرنت انتقال وجه پایا', '/ach', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('F7129CBE-AB42-43A1-B827-81AB2C741D3F', 'SVC_RTGS_XFER_PARENT', 'پرنت انتقال وجه ساتنا', '/rtgs', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('5C421B78-3AEB-4F1A-A219-21C3BD87AE2F', 'SVC_IP_XFER_PARENT', 'پرنت انتقال وجه پل', '/ip', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');


--------------------------------------------- PICHACK_MANAGEMENT_SERVICES ----------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 'SVC_PICHACK_PARENT', 'پرنت پیچک', '/pichack', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, PARENT_SERVICE_ID,
                                 SERVICE_TYPE_CODE, REQUEST_JSON_SCHEMA, RESPONSE_JSON_SCHEMA,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('F21A3BED-A23B-432E-A21F-32EF1ABC2DEA', 'SVC_PICHACK_CHEQUE_REGISTER', 'سرویس ثبت چک', '/issue', 1, 1,
        'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 5,
        'ir.daneshrefah.scm.plugin.pichack.provider.dto.register.PichackChequeRegisterRequest',
        'ir.daneshrefah.scm.plugin.pichack.provider.dto.register.PichackChequeRegisterResponse',
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, '{"serviceName":"/cheque/issue"}', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('8DFA1C2E-A13D-421B-A2EF-23EFABC1DEC', 'SVC_PICHACK_CHEQUE_CONFIRM_BY_RECEIVER',
        'سرویس تایید یا رد چک توسط گیرنده', '/accept', 1, 1, REQUEST_JSON_SCHEMA,
        'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 5,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('3EFA2B3D-3C2A-412B-A23C-12FA3C1BEDA', 'SVC_PICHACK_CHEQUE_TRANSFER', 'سرویس انتقال چک', '/transfer', 1, 1,
        REQUEST_JSON_SCHEMA, 'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 1,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('E2FA2B3D-2A1C-421B-A23C-12FA3C1BEDA', 'SVC_PICHACK_CHEQUE_INQUIRY_BY_HOLDER', 'سرویس استعلام چک توسط دارنده',
        '/inquiry-cheque', 1, 1, REQUEST_JSON_SCHEMA, 'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 1,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('8B724CCE-24D1-45E3-B9E6-AC2BDFE82B28', 'SVC_PICHACK_CHEQUE_INQUIRY_BY_ISSUER',
        'سرویس استعلام چک توسط صادرکننده', '/issuer-inquiry', 1, 1, REQUEST_JSON_SCHEMA,
        'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 1,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('1F13241A-712B-42D2-A928-22178C8A172E', 'SVC_PICHACK_CHEQUE_INQUIRY_CHECK_RECEIVER',
        'استعلام نام دریافت کننده چک', '/receiver-inquiry', 1, 1, REQUEST_JSON_SCHEMA,
        'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 1,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('9F412783-D72A-4F2A-B1C3-128A427C8A9B', 'SVC_PICHACK_CHEQUE_INQUIRY_BY_CHECK_PARAM',
        'سرویس استعلام چک بر اساس اقلام چک', '/dynamic-info-inquiry', 1, 1, REQUEST_JSON_SCHEMA,
        'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 1,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, METADATA, CREATOR, LAST_EDITOR)
VALUES ('021ABF3D-4F32-4F2E-A98C-B4721C34221A', 'SVC_PICHACK_CHEQUE_INQUIRY_BY_TRANSFERS_CHAIN',
        'سرویس استعلام زنجیره انتقالات و ذینفعان', '/transfers-chain', 1, 1, REQUEST_JSON_SCHEMA,
        'F2AB3CEA-2C1D-412A-B23F-321FA1BC2DEA', 1,
        1, '8FA1148A-A679-4F4D-B92E-7312F23A1BEC', 1, METADATA, 'Reza Jamshidi', 'Reza Jamshidi');


--------------------------------------------- ???????_MANAGEMENT_SERVICES ----------------------------------------------

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('2a118618-b56d-4187-b765-6c110d2a91d6', 'SVC_CUSTOMER_PARENT', 'مدیریت اطلاعات مشتریان', '/customer', 1, 1, 4,
        5, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('2F4A12BE-C3DE-412B-A1FE-12BEFA321DEC', 'SVC_BASE_INFO_PARENT', 'پرنت اطلاعات پایه', '/base-info', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('7A1C23EF-B23D-42AB-B21A-32BEFA1C2DEA', 'SVC_LOAN_PARENT', 'پرنت تسهیلات', '/loan', 1, 1, 4, 5, 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('B1EF2A3C-1D2A-421F-A12C-23EFABC12DEA', 'SVC_CHEQUE_ORDER_PARENT', 'پرنت درخواست دسته چک', '/cheque-order', 1,
        1, 4, 5, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('0EF13B2A-A12D-412E-B23B-23FA1C2BEAD', 'SVC_BILL_PARENT', 'پرنت قبض', '/bill', 1, 1, 4, 5, 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('9C2D3FEA-A32B-432F-A21E-32EF1ABC2DEC', 'SVC_XFER_PARENT', 'پرنت انتقال وجه', '/xfer', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('E1FA2B3D-321A-412B-A23C-12FA3C2BEDA', 'SVC_CARD_MANAGEMENT_PARENT', 'پرنت مدیریت کارت', '/card-manage', 1, 1,
        4, 5, 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('4AB12C3E-1BFA-421D-B12A-2BEFA3C1DECA', 'SVC_CARD_PAYMENT_PARENT', 'پرنت پرداخت با کارت', '/card', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('5D2C1AEF-B23A-432E-A1FE-31EF2ABC1DEA', 'SVC_TOPUP_PARENT', 'پرنت شارژ و بسته', '/card', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('8BFA1C2E-A13D-421B-A2EF-23EFABC1DEC', 'SVC_CHACKAD_PARENT', 'پرنت چکاد', '/chackad', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('1A2C3BED-A21B-432A-B12F-23EF1A1CBEC', 'SVC_PETTY_CASH_PARENT', 'پرنت مدیریت تنخواه', '/petty-cash', 1, 1, 4, 5,
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('C3EFAB2D-B12A-412F-A23E-32FA1CBECAD', 'SVC_PAYMENT_ORDER_PARENT', 'پرنت دستور پرداخت ویژه', '/payment-order',
        1, 1, 4, 5, 'Reza Jamshidi', 'Reza Jamshidi');


---------------------------------------------- BUNDLE MANAGEMENT SERVICE -----------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('24a5aaaf-a92e-4896-a873-39e4ff75a420', 'SVC_BUNDLE_PARENT', 'پرنت باندل', '/bundle', 1, 1, 4, 5,
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('bd0fe44f-68f2-4952-8850-fb15b05f0761', 'SVC_BUNDLE_LIST', 'لیست باندل', '/list', 1, 1, 1, 2, 1,
        'bean:bundleManagementService.bundleList(ir.daneshrefah.scm.common.dto.bundle.BundleFindRequest)',
        '24a5aaaf-a92e-4896-a873-39e4ff75a420', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('d2ae1a02-4b8a-49cf-a067-49127cd8204c', 'SVC_BUNDLE_LIST_FIND_BY_ID', 'بازیابی اطلاعات باندل با شناسه',
        '/{id}', 1, 1, 3, 2, 1,
        'bean:bundleManagementService.findById(String)', '24a5aaaf-a92e-4896-a873-39e4ff75a420',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('e4f895a9-41c9-47db-9ea0-c46362f8dfa2', 'SVC_BUNDLE_EDIT', 'ویرایش باندل', '/edit', 1, 1, 1, null, null,
        '24a5aaaf-a92e-4896-a873-39e4ff75a420', 6, 2,
        'bean:bundleManagementService.edit(ir.daneshrefah.scm.common.dto.bundle.BundleEditRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-17 10:29:03.460568', '2024-04-17 10:29:03.460568',
        'Dariush Abdolahi', 'Dariush Abdolahi');

----------------------------------------------- ERROR MANAGEMENT -------------------------------------------------------

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('f0d90091-3673-4f18-a77f-c81463782b3b', 'SVC_ERROR_PARENT', 'پرنت خطا', '/error', 1, 1, 4, 5,
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a6e60c29-8a32-4a1d-b355-f8091090a9e8', 'SVC_ERROR_LIST', 'لیست خطاها', '/list', 1, 1, 1, 2, 1,
        'bean:exceptionManagementService.list(ir.daneshrefah.scm.common.dto.error.ErrorMappingFindRequest)',
        'f0d90091-3673-4f18-a77f-c81463782b3b', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('1a84e734-b7ef-4173-bda2-7f49b4cf130f', 'SVC_ERROR_FIND_BY_ID', 'بازیابی اطلاعات خطا با شناسه',
        '/{id}', 1, 1, 3, 2, 1,
        'bean:exceptionManagementService.findById(String)', 'f0d90091-3673-4f18-a77f-c81463782b3b',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('a637116f-6bc1-47a4-80a1-8cf6f65edf57', 'SVC_ERROR_EDIT', 'ویرایش خطا', '/edit', 1, 1, 1, null, null,
        'f0d90091-3673-4f18-a77f-c81463782b3b', 6, 2,
        'bean:exceptionManagementService.edit(ir.daneshrefah.scm.common.dto.error.ErrorMappingEditRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-17 10:29:03.460568', '2024-04-17 10:29:03.460568',
        'Dariush Abdolahi', 'Dariush Abdolahi');

--------------------------------------------- DYNAMIC REST MANAGEMENT SERVICE -----------------------------------------------
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('04dfb077-7539-46a8-a4d5-0548ef986867', 'SVC_DYNAMIC_REST_PARENT', 'پرنت API سرویس های REST', '/dynamic-rest',
        1, 1, 4, 5, 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('7c1d4beb-45d6-4b54-81be-7c6d3f3f63f8', 'SVC_REST_PROVIDER_NAME_LIST', 'لیست سرویس های مرجع REST', '/provider-names', 1, 1, 1, 2, 1,
        'bean:dynamicRestManagementService.getRestProviderNameList()',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('f96821fa-ccac-491b-9624-02db31bc9861', 'SVC_PARAMETER_CREATE', 'ایجاد پارامتر', '/parameter', 1, 1, 5, 2, 1,
        'bean:dynamicRestManagementService.createParameter(ir.daneshrefah.scm.common.dto.rest.ParameterCreateRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('743f6bce-0306-4dd8-a671-1398b716532f', 'SVC_CONDITION_DATA_SOURCE_CREATE', 'ایجاد دیتاسورس شرط پاسخ دهی سرویس', '/data-source', 1, 1, 5, 2, 1,
        'bean:dynamicRestManagementService.createResponseConditionDatasource(ir.daneshrefah.scm.common.dto.rest.ResponseConditionDatasourceRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('8cf1b268-dc93-43ba-8679-df6fa68c7620', 'SVC_RESPONSE_CONDITION_CREATE', 'ایجاد شرط پاسخ دهی سرویس', '/response-condition', 1, 1, 5, 2, 1,
        'bean:dynamicRestManagementService.createResponseCondition(ir.daneshrefah.scm.common.service.rest.ResponseConditionRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');


INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('5becbcf4-c2a1-45b1-819b-2432dc3c9c12', 'SVC_PARAMETER_CHANGE', 'ویرایش پارامتر', '/parameter', 1, 1, 6, 2, 1,
        'bean:dynamicRestManagementService.changeParameter(ir.daneshrefah.scm.common.dto.rest.ParameterChangeRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('1cfb8e2b-fd67-4bf4-9b19-373b4877be3b', 'SVC_RESPONSE_CONDITION_CHANGE', 'ویرایش شرط پاسخ دهی سرویس', '/response-condition', 1, 1, 6, 2, 1,
        'bean:dynamicRestManagementService.changeResponseCondition(ir.daneshrefah.scm.common.service.rest.ResponseConditionChangeRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('8e10018a-eb27-44a8-9c7d-2cf4ed1e5b93', 'SVC_CONDITION_DATA_SOURCE_CHANGE', 'ویرایش دیتاسورس شرط پاسخ دهی سرویس', '/data-source', 1, 1, 6, 2, 1,
        'bean:dynamicRestManagementService.changeResponseConditionDatasource(ir.daneshrefah.scm.common.dto.rest.ResponseConditionDatasourceChangeRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('95d2b762-5ad8-4c3c-bb7b-e1097d3c43be', 'SVC_PARAMETER_DELETE', 'حذف پارامتر', '/parameter', 1, 1, 1, null, null,
        '04dfb077-7539-46a8-a4d5-0548ef986867', 7, 2,
        'bean:dynamicRestManagementService.removeParameter(ir.daneshrefah.scm.common.dto.rest.ParameterDeleteRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-16 13:27:10.000000', '2024-04-16 13:27:12.000000',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('87ee23d3-4996-44b9-ba14-0d983a6e9f7d', 'SVC_RESPONSE_CONDITION_DELETE', 'حذف شرط پاسخ دهی سرویس', '/response-condition', 1, 1, 1, null, null,
        '04dfb077-7539-46a8-a4d5-0548ef986867', 7, 2,
        'bean:dynamicRestManagementService.removeResponseCondition(ir.daneshrefah.scm.common.service.rest.ResponseConditionDeleteRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-16 13:27:10.000000', '2024-04-16 13:27:12.000000',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, IMPLEMENTATION_SERVICE_PROVIDER_ID,
                                 IMPLEMENTATION_BPMN_CONTENT, IMPLEMENTATION_COMPOSITION_TYPE_CODE,
                                 CHECK_ACCESS_FIRST_AUTHENTICATION, CHECK_ACCESS_SECOND_AUTHENTICATION,
                                 CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET, PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET,
                                 PROPERTY_NAME_CUSTOMER, METADATA, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR)
VALUES ('10faa773-0b5a-45f7-812d-ca5602747ba8', 'SVC_CONDITION_DATA_SOURCE_DELETE', 'حذف دیتاسورس شرط پاسخ دهی سرویس', '/data-source', 1, 1, 1, null, null,
        '04dfb077-7539-46a8-a4d5-0548ef986867', 7, 2,
        'bean:dynamicRestManagementService.removeResponseConditionDatasource(ir.daneshrefah.scm.common.dto.rest.ResponseConditionDatasourceRemoveRequest)', null,
        null, null, 1, 0, 0, 0, null, null, null, null, '2024-04-16 13:27:10.000000', '2024-04-16 13:27:12.000000',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('8a51107a-0b3f-4e2b-9e1f-2cd34b0031ea', 'SVC_PARAMETER_FIND_BY_ID', 'یافتن پارامتر با شناسه', '/parameter/{id}', 1, 1,
        3, 2, 1,
        'bean:dynamicRestManagementService.findParameterById(String)', '04dfb077-7539-46a8-a4d5-0548ef986867',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('77646c1b-3d42-43bb-9c94-c258e3db939e', 'SVC_PARAMETER_FIND', 'لیست پارامتر', '/parameter/find', 1, 1, 1, 2, 1,
        'bean:dynamicRestManagementService.findParameter(ir.daneshrefah.scm.common.dto.rest.ParameterFindRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('193c26ce-5daf-44dc-90eb-d871a0a61fac', 'SVC_PARAMETER_FIND_TREE', 'درخت پارامتر', '/parameter/find-tree', 1, 1, 1, 2, 1,
        'bean:dynamicRestManagementService.findParameterTree(ir.daneshrefah.scm.common.dto.rest.ParameterTreeFindRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('35f45d53-3991-4752-8ae9-bac35dd8ba39', 'SVC_RESPONSE_CONDITION_FIND', 'لیست شرط پاسخ دهی سرویس', '/response-condition/find', 1, 1, 1, 2, 1,
        'bean:dynamicRestManagementService.findResponseCondition(ir.daneshrefah.scm.common.service.rest.ResponseConditionFindRequest)',
        '04dfb077-7539-46a8-a4d5-0548ef986867', 'Dariush Abdolahi', 'Dariush Abdolahi');

--------------------------------------------- CUSTOMER MANAGEMENT SERVICE -----------------------------------------------

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CREATOR, LAST_EDITOR)
VALUES ('1b5ac43d-3f44-4201-bd36-952ee2392bb7', 'SVC_ASSETS_PARENT', 'پرنت دارایی ها', '/assets',
        1, 1, 4, 5, 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a1c7053d-9902-4f60-b597-0e6e39273da5', 'SVC_ASSETS_LIST', 'لیست دارایی های یک کاربر', '/list', 1, 1, 1, 2, 1,
        'bean:customerManagementService.findProviderMembershipList(ir.daneshrefah.scm.common.dto.membership.CustomerProviderFindRequest)',
        '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('e181c6c3-06b5-44eb-a197-c9c38892423d', 'SVC_ASSETS_LIST_LOCAL', 'لیست لوکال دارایی های یک کاربر', '/local-list', 1, 1, 1, 2, 1,
        'bean:customerManagementService.findLocalMembershipList(ir.daneshrefah.scm.common.dto.membership.MembershipFindRequest)',
        '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, STATUS, IS_SYSTEMIC, SERVICE_TYPE_CODE,
                                 SERVICE_IMPLEMENTATION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTHENTICATION,
                                 IMPLEMENTATION_JAVA_CLASS_NAME, PARENT_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('9c13ebd9-ac83-4c8a-82bf-494577067fc8', 'SVC_ASSETS_FIND_ACCOUNT_MEMBERSHIP', 'یافتن دارایی اکانت با شناسه', '/local-account/{membershipId}', 1, 1,
        3, 2, 1,
        'bean:customerManagementService.findAccountMembershipById(String)', '1b5ac43d-3f44-4201-bd36-952ee2392bb7',
        'Dariush Abdolahi', 'Dariush Abdolahi');



CREATE TABLE REF.TBL_SCM_TERMINAL_SERVICE_ACCESS
(
    TERMINAL_SERVICE_ACCESS_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    TERMINAL_ID                VARCHAR(36) NOT NULL,
    SERVICE_ID                 VARCHAR(36) NOT NULL,
    CREATE_DATE                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                    VARCHAR(255),
    LAST_EDITOR                VARCHAR(255),
    CONSTRAINT CNST_UNIQUE_TERMINAL_SERVICE UNIQUE (TERMINAL_ID, SERVICE_ID),
    FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
    FOREIGN KEY (SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    PRIMARY KEY (TERMINAL_SERVICE_ACCESS_ID)
);

INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', '41088034-ad79-4f6a-9e04-4f6a31374148', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_NAB_CUSTOMER_ACCOUNT_LIST' to 'IB'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', '5EF23AB1-2A1C-421F-A12E-23EFABC1DEC', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_NAB_ACCOUNT_WITHDRAW_TABLE' to 'IB'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', '45bcf9af-2614-4a2e-933a-3121c3fb2c06', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_ACCOUNT_WITHDRAW_TABLE' to 'IB'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', 'F12A3BED-A32B-432E-A21F-31EF1ABC2DEA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_USER_LOCAL_ACCOUNT_LIST' to 'IB'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', 'F21A3BED-A23B-432E-A21F-32EF1ABC2DEA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_PICHACK_CHEQUE_REGISTER' to 'IB'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', 'DE3A812C-B741-401B-A012-8C4271A2BDEF', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_INTERNAL_XFER_ADD' to 'IB'


INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'bd0fe44f-68f2-4952-8749-fb15b05f0760', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'E1FA2B3D-212A-421B-A23C-12FA3C1BEDA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_FIND_BY_ID' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'd42c6df9-e27d-4894-b86c-a48dd2a11412', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_CREATE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '3AB12C3E-1CFA-412B-A23C-12FA3C2BEDA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_EDIT' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '2BFA1C2E-A31D-412A-B23F-23EFABC1DEC', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_DELETE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '44fddd2a-5df4-4a8a-a4c2-5381ef673e02', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_ADD_SERVICE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '7D2C1AEF-A23A-4B2E-A1FE-32EF2ABC1DEA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_TERMINAL_DELETE_SERVICE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '790f7095-0102-44a7-9802-8d2530f09033', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_SERVICE_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'd2220c12-30be-4305-9706-91d699826731', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_SERVICE_BY_CODE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'ee6380f7-3f2d-4751-8211-74a67370635e', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_SERVICE_CREATE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '7B1A2C3E-21FA-421C-B23D-12FA3C1BEDA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_CHANNEL_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '5EF13AB1-2B1C-421F-A12E-23EFABC1DEC', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_CHANNEL_FIND_BY_ID' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'DB1A2C3E-2BFA-421C-B12E-12FA3C1BEDA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_CHANNEL_CREATE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '0C2E3FEA-A12C-432F-A21F-12EFABC1DEC', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_CHANNEL_EDIT' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'AB3F2C3E-A12D-421A-B23B-23FA3C2BEAD', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_CHANNEL_DELETE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '41088034-ad79-4f6a-9e04-4f6a31374148', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add 'SVC_NAB_CUSTOMER_ACCOUNT_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '819da0df-a024-4c3a-93b1-120a79084c27', '2024-04-16 13:41:53.000000',
        '2024-04-16 13:41:56.000000', 'Dariush Abdolahi', 'Dariush Abdolahi'); -- add 'SVC_SERVICE_DELETE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '455d9247-e05c-49e6-b504-ad9bf2b9241c', '2024-04-17 14:15:03.000000',
        '2024-04-17 14:15:05.000000', 'Dariush Abdolahi', 'Dariush Abdolahi'); -- add 'SVC_SERVICE_EDIT' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '33f6f9aa-d651-447a-a5e5-8a80d2941f57', '2024-04-22 15:11:06.000000',
        '2024-04-22 15:11:08.000000', 'Dariush Abdolahi', 'Dariush Abdolahi');-- add 'SVC_TERMINAL_ACCESS_SERVICE_LIST to 'SCM
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '69d6a8e0-f090-4e41-971f-a03b4a6b388f', '2024-04-22 13:39:10.000000',
        '2024-04-22 13:39:13.000000', 'Dariush Abdolahi', 'Dariush Abdolahi');-- add 'SVC_TERMINAL_ACCESS_TERMINAL_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'bd0fe44f-68f2-4952-8850-fb15b05f0761', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_BUNDLE_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'd2ae1a02-4b8a-49cf-a067-49127cd8204c', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_BUNDLE_FIND_BY_ID' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'e4f895a9-41c9-47db-9ea0-c46362f8dfa2', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_BUNDLE_EDIT' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'a6e60c29-8a32-4a1d-b355-f8091090a9e8', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_ERROR_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '1a84e734-b7ef-4173-bda2-7f49b4cf130f', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_ERROR_FIND_BY_ID' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'a637116f-6bc1-47a4-80a1-8cf6f65edf57', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_ERROR_EDIT' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'a1c7053d-9902-4f60-b597-0e6e39273da5', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_ASSETS_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'e181c6c3-06b5-44eb-a197-c9c38892423d', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_ASSETS_LIST_LOCAL' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '9c13ebd9-ac83-4c8a-82bf-494577067fc8', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_ASSETS_FIND_ACCOUNT_MEMBERSHIP' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '7c1d4beb-45d6-4b54-81be-7c6d3f3f63f8', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_REST_PROVIDER_NAME_LIST' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'f96821fa-ccac-491b-9624-02db31bc9861', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_PARAMETER_CREATE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '8cf1b268-dc93-43ba-8679-df6fa68c7620', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_RESPONSE_CONDITION_CREATE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '743f6bce-0306-4dd8-a671-1398b716532f', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_CONDITION_DATA_SOURCE_CREATE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '5becbcf4-c2a1-45b1-819b-2432dc3c9c12', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_PARAMETER_CHANGE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '1cfb8e2b-fd67-4bf4-9b19-373b4877be3b', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_RESPONSE_CONDITION_CHANGE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '8e10018a-eb27-44a8-9c7d-2cf4ed1e5b93', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_CONDITION_DATA_SOURCE_CHANGE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '95d2b762-5ad8-4c3c-bb7b-e1097d3c43be', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_PARAMETER_DELETE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '87ee23d3-4996-44b9-ba14-0d983a6e9f7d', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_RESPONSE_CONDITION_DELETE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '10faa773-0b5a-45f7-812d-ca5602747ba8', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_CONDITION_DATA_SOURCE_DELETE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '8a51107a-0b3f-4e2b-9e1f-2cd34b0031ea', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_PARAMETER_FIND_BY_ID' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '77646c1b-3d42-43bb-9c94-c258e3db939e', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_PARAMETER_FIND' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '193c26ce-5daf-44dc-90eb-d871a0a61fac', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_PARAMETER_FIND_TREE' to 'SCM'
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '35f45d53-3991-4752-8ae9-bac35dd8ba39', 'Dariush Abdolahi',
        'Dariush Abdolahi'); -- add 'SVC_RESPONSE_CONDITION_FIND' to 'SCM'




CREATE TABLE REF.TBL_SCM_SERVICE_RELATION
(
    SERVICE_RELATION_ID       DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    SOURCE_SERVICE_ID         VARCHAR(36) NOT NULL,
    ORDER                     INTEGER,
    TARGET_SERVICE_ID         VARCHAR(36) NOT NULL,
    TARGET_SERVICE_COMMIT_ID  VARCHAR(36),
    TARGET_SERVICE_REVERSE_ID VARCHAR(36),
    CREATE_DATE               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                   VARCHAR(255),
    LAST_EDITOR               VARCHAR(255),
    FOREIGN KEY (SOURCE_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    FOREIGN KEY (TARGET_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    FOREIGN KEY (TARGET_SERVICE_COMMIT_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    FOREIGN KEY (TARGET_SERVICE_REVERSE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    CONSTRAINT CNST_UNIQUE_SERVICE_RELATION UNIQUE (SOURCE_SERVICE_ID, TARGET_SERVICE_ID),
    PRIMARY KEY (SERVICE_RELATION_ID)
);
INSERT INTO REF.TBL_SCM_SERVICE_RELATION (SOURCE_SERVICE_ID, ORDER, TARGET_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('45bcf9af-2614-4a2e-933a-3121c3fb2c06', 1, '5EF23AB1-2A1C-421F-A12E-23EFABC1DEC', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add SVC_NAB_ACCOUNT_WITHDRAW_TABLE to SVC_ACCOUNT_WITHDRAW_TABLE
INSERT INTO REF.TBL_SCM_SERVICE_RELATION (SOURCE_SERVICE_ID, ORDER, TARGET_SERVICE_ID, CREATOR, LAST_EDITOR)
VALUES ('6c2fd7ae-4558-44f3-8b27-96af58eca374', 1, 'd2214447-60df-4807-8e66-f650b6a15bca', 'Reza Jamshidi',
        'Reza Jamshidi'); -- add SVC_NAB_ACCOUNT_LIST_PROXY to SVC_USER_ACCOUNT_LIST_PROXY

ALTER TABLE REF.CUSTOMER
    ADD COLUMN SERVICE_PROVIDER_ID VARCHAR(36) NOT NULL;
ALTER TABLE REF.CUSTOMER
    ADD CONSTRAINT FK_CUSTOMER_SERVICE_PROVIDER_ID FOREIGN KEY (SERVICE_PROVIDER_ID)
        REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID) ON DELETE NO ACTION ON UPDATE NO ACTION;
UPDATE REF.CUSTOMER
SET SERVICE_PROVIDER_ID = '3ce3e10e-c3cd-49c7-ae5c-330a81e882d7'; -- update all providers to 'NAB'

ALTER TABLE REF.MEMBERSHIP
    ADD COLUMN ASSET_TYPE INT NOT NULL default 1;
UPDATE REF.MEMBERSHIP
SET ASSET_TYPE = 1; -- update all asset types to 'ACCOUNT'


CREATE TABLE REF.TBL_SCM_TRANSFORMER
(
    TRANSFORMER_ID  VARCHAR(36) NOT NULL,
    TITLE           VARCHAR(255),
    METADATA        VARCHAR(1000),
    JAVA_CLASS_NAME VARCHAR(255),
    CREATE_DATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR         VARCHAR(255),
    LAST_EDITOR     VARCHAR(255),
    PRIMARY KEY (TRANSFORMER_ID)
);
INSERT INTO REF.TBL_SCM_TRANSFORMER (TRANSFORMER_ID, TITLE, METADATA, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('8C2D1FEA-B13A-421E-A12C-23EFABC1DEC', 'فیلتر لیست حساب', '', 'bean:accountListResponseTransformer',
        'Reza Jamshidi', 'Reza Jamshidi');



CREATE TABLE REF.TBL_SCM_TRANSFORMER_RELATION
(
    TRANSFORMER_RELATION_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    RELATION_TYPE_CODE      INTEGER     NOT NULL,
    ORDER                   SMALLINT,
    METADATA                VARCHAR(1000),
    TRANSFORMER_ID          VARCHAR(36) NOT NULL,
    SOURCE_ID               VARCHAR(36) NOT NULL,
    CREATE_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                 VARCHAR(255),
    LAST_EDITOR             VARCHAR(255),
    CONSTRAINT CNST_UNIQUE_TRANSFORMER_RELATION UNIQUE (TRANSFORMER_ID, SOURCE_ID, RELATION_TYPE_CODE),
    FOREIGN KEY (TRANSFORMER_ID) REFERENCES REF.TBL_SCM_TRANSFORMER (TRANSFORMER_ID),
    PRIMARY KEY (TRANSFORMER_RELATION_ID)
);

INSERT INTO REF.TBL_SCM_TRANSFORMER_RELATION (RELATION_TYPE_CODE, ORDER, METADATA, TRANSFORMER_ID, SOURCE_ID, CREATOR,
                                              LAST_EDITOR)
VALUES (2, 1, null, '8C2D1FEA-B13A-421E-A12C-23EFABC1DEC', 'F12A3BED-A32B-432E-A21F-31EF1ABC2DEA', 'Reza Jamshidi',
        'Reza Jamshidi'); -- ADD 'ACCOUNT_LIST_RESPONSE_TRANSFORMER' TO 'SVC_USER_LOCAL_ACCOUNT_LIST'

INSERT INTO REF.TBL_SCM_TRANSFORMER_RELATION (RELATION_TYPE_CODE, ORDER, METADATA, TRANSFORMER_ID, SOURCE_ID, CREATOR,
                                              LAST_EDITOR)
VALUES (4, 1, null, '8C2D1FEA-B13A-421E-A12C-23EFABC1DEC', '6c2fd7ae-4558-44f3-8b27-96af58eca374', 'Reza Jamshidi',
        'Reza Jamshidi'); -- ADD 'ACCOUNT_LIST_RESPONSE_TRANSFORMER' TO 'SVC_USER_ACCOUNT_LIST_PROXY'



CREATE TABLE REF.TBL_SCM_PERSON_SERVICE_ACCESS
(
    PERSON_SERVICE_ACCESS_ID DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    PERSON_PROFILE_ID        VARCHAR(10) NOT NULL,
    SERVICE_ID               VARCHAR(36) NOT NULL,
    TERMINAL_ID              VARCHAR(36),
    ASSET_ID                 VARCHAR(100),
    CREATE_DATE              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                  VARCHAR(255),
    LAST_EDITOR              VARCHAR(255),
    FOREIGN KEY (SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
    FOREIGN KEY (PERSON_PROFILE_ID) REFERENCES REF.USER (USERNAME),
    PRIMARY KEY (PERSON_SERVICE_ACCESS_ID)
);

-- begin survey tables

CREATE TABLE REF.TBL_SCM_SURVEY_SUBJECT
(
    SURVEY_SUBJECT_ID DECIMAL(22)  NOT NULL GENERATED ALWAYS AS IDENTITY,
    NAME              VARCHAR(255) NOT NULL,
    IS_WEAKNESS       SMALLINT     NOT NULL DEFAULT 1,
    TITLE_FA          VARCHAR(255) NOT NULL,
    TITLE_EN          VARCHAR(255) NOT NULL,
    CREATE_DATE       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CREATOR           VARCHAR(255),
    LAST_EDITOR       VARCHAR(255),
    PRIMARY KEY (SURVEY_SUBJECT_ID)
);

CREATE TABLE REF.TBL_SCM_SURVEY_TITLE
(
    SURVEY_TITLE_ID DECIMAL(22)  NOT NULL GENERATED ALWAYS AS IDENTITY,
    NAME            VARCHAR(255) NOT NULL,
    TITLE_FA        VARCHAR(255) NOT NULL,
    TITLE_EN        VARCHAR(255) NOT NULL,
    CREATE_DATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR         VARCHAR(255),
    LAST_EDITOR     VARCHAR(255),
    PRIMARY KEY (SURVEY_TITLE_ID)
);

CREATE TABLE REF.TBL_SCM_SURVEY_RESULT
(
    SURVEY_RESULT_ID DECIMAL(22)   NOT NULL GENERATED ALWAYS AS IDENTITY,
    SURVEY_TITLE_ID  DECIMAL(22)   NOT NULL,
    TERMINAL_ID      VARCHAR(36)   NOT NULL,
    CLIENT_ID        DECIMAL(22),
    RATE             SMALLINT      NOT NULL,
    COMMENT          VARCHAR(2500) NOT NULL,
    CREATE_DATE      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR          VARCHAR(255),
    FOREIGN KEY (SURVEY_TITLE_ID) REFERENCES REF.TBL_SCM_SURVEY_TITLE (SURVEY_TITLE_ID),
    FOREIGN KEY (TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
    FOREIGN KEY (CLIENT_ID) REFERENCES REF.TBL_SUA_CLIENT (CLIENT_ID),
    PRIMARY KEY (SURVEY_RESULT_ID)
);

CREATE TABLE REF.TBL_SCM_SURVEY_RESULT_SUBJECT
(
    SURVEY_RESULT_SUBJECT_ID DECIMAL(22) GENERATED ALWAYS AS IDENTITY,
    SURVEY_RESULT_ID         DECIMAL(22) NOT NULL,
    SURVEY_SUBJECT_ID        DECIMAL(22) NOT NULL,
    CREATE_DATE              TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                  VARCHAR(255),
    FOREIGN KEY (SURVEY_RESULT_ID) REFERENCES REF.TBL_SCM_SURVEY_RESULT (SURVEY_RESULT_ID),
    FOREIGN KEY (SURVEY_SUBJECT_ID) REFERENCES REF.TBL_SCM_SURVEY_SUBJECT (SURVEY_SUBJECT_ID),
    UNIQUE (SURVEY_RESULT_ID, SURVEY_SUBJECT_ID),
    PRIMARY KEY (SURVEY_RESULT_SUBJECT_ID)
);

-- end survey tables

CREATE TABLE REF.TBL_SCM_RESOURCE_BUNDLE
(
    RESOURCE_BUNDLE_ID DECIMAL(22) GENERATED ALWAYS AS IDENTITY
        CONSTRAINT PK_TBL_SCM_RESOURCE_BUNDLE PRIMARY KEY,
    KEY                VARCHAR(255) NOT NULL,
    LOCALE_CODE        VARCHAR(32)  NOT NULL,
    VALUE              VARCHAR(255),
    CREATOR            VARCHAR(255) NOT NULL,
    LAST_EDITOR        VARCHAR(255) NOT NULL,
    CREATE_DATE        TIMESTAMP(6) NOT NULL,
    LAST_EDIT_DATE     TIMESTAMP(6) NOT NULL
);

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:47.000000',
        '2024-06-02 12:45:46.000000', 'fa-IR', 'ex::default', 'خطای ناشناخته');
INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:48:32.000000',
        '2024-06-02 12:48:30.000000', 'ar-AE', 'ex::default', 'خطأ غير معروف');
INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::default', 'unknown exception');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException',
        'service by code '':serviceCode'' with terminal code '':terminalCode'' not found.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException',
        'سرویس با کد '':serviceCode'' در ترمینال با کد '':terminalCode'' یافت نگردید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.TooManyRecordFoundException',
        'too many (:count) :source found.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.TooManyRecordFoundException',
        'بیش از یک رکورد (:count) برای :source یافت شد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException',
        'no :providerCode [:source] found.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException',
        'رکوردی برای [:source] در سرویس مقصد :providerCode یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.NoDataChangedException',
        ':source data has no change');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.NoDataChangedException',
        'خطا در ویرایش  :source');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.NoCustomerFoundException',
        'no customer found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.NoCustomerFoundException',
        'مشتری مورد نظر یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.NoAssetFoundException',
        'no asset found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.NoAssetFoundException',
        'دارایی  های فرد یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.MissingRequiredInputException',
        ':source is empty.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.MissingRequiredInputException',
        'فیلد :source خالی میباشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.MissingRequestException',
        'request body is null');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.MissingRequestException',
        'محتوای بدنه درخواست ارسالی خالی می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.MethodNotSupportedException',
        ':source');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.MethodNotSupportedException',
        ' :source پشتیبانی نمی شود');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.MethodNotSupportDataException',
        ':source');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.MethodNotSupportDataException',
        'خطا در :source ');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.InvalidRequestFormatException',
        ':message');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.InvalidRequestFormatException',
        'فرمت ارسالی :message صحیح نمی باشد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.InvalidRemoteResponseException',
        ':providerCode (:source) is invalid.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.InvalidRemoteResponseException',
        '(:source) برای سرویس مقصد :providerCode اشتباه است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.InvalidInputException',
        ':source is invalid.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.InvalidInputException',
        'داده ورودی :source اشتباه است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.InvalidInputDateFormatException',
        ':source is invalid.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.InvalidInputDateFormatException',
        'فرمت ارسالی :source اشتباه است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.InputMismatchException',
        'mismatch input count,:parameterCount input required, but :inputCount received.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.InputMismatchException',
        'تعداد ورودی های ارسالی :inputCount عدد می باشد،تعداد مورد نیاز :parameterCount عدد می باشد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.InputAlreadyExistException',
        ':source already exist.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.InputAlreadyExistException',
        ' :source از قبل وجود دارد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException',
        ':source is duplicated.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException',
        ' :source تکراری می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.AuthenticationRequiredException',
        'authentication required.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.AuthenticationRequiredException',
        'پردازش مورد نظر نیازمند به احراز هویت می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.TerminalNotAssignedServiceException',
        'service :serviceCode not assigned to terminal :terminalCode');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.TerminalNotAssignedServiceException',
        'سرویس :serviceCode به ترمینال :terminalCode دارای ارتباط نمی باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.ServiceInvalidMetadataException',
        'service [:serviceCode] has invalid metadata');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.ServiceInvalidMetadataException',
        'سرویس [:serviceCode] دارای فراداده اشتباه می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.RecordVersionException',
        'record version does not match');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.RecordVersionException',
        'خطا در نسخه رکورد (رکورد پیش از ثبت تغیرات شما دچار تغییرات گردیده است)');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.MessagePayloadMergeException',
        'incompatible array type of payloads');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.MessagePayloadMergeException',
        'نوع آرایه بدنه پیام نا متناسب می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.InvalidDelegationException',
        'user: :username , does not have delegation authority');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.InvalidDelegationException',
        'کاربر :username اجازه تفویض اختیار ندارد ');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.DisableServiceExecutionException',
        'service :serviceCode has been disabled');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.DisableServiceExecutionException',
        'سرویس :serviceCode غیرفعال گردیده است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'exp.dyn.person.not.found.id',
        'person with id :id not found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'exp.dyn.person.not.found.id',
        'کاربر با شناسه :id یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'exp.dyn.person.not.found',
        'could not found person');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'exp.dyn.person.not.found',
        'کاربر یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'exp.dyn.person.not.found.nick.name.terminal',
        'person with nickname :nickname and terminalCode :terminalCode not found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'exp.dyn.person.not.found.nick.name.terminal',
        'کاربری با نام :nickname در ترمینال :terminalCode یافت نگردید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.plugin.api.exception.TransformException',
        'error on transform data');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.plugin.api.exception.TransformException',
        'خطا در تبدیل داده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.notification.client.exception.EmptyNotificationRequestException',
        ':property is empty');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.notification.client.exception.EmptyNotificationRequestException',
        ' :property خالی می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.notification.client.exception.InvalidNotificationRequestException',
        ':property is invalid');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.notification.client.exception.InvalidNotificationRequestException',
        ' :property اشتباه می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotFoundSupportedBodyProcessorException',
        'notification body processor not found for template code : :templateCode');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotFoundSupportedBodyProcessorException',
        'پردازشگر نوتیفیکشین برای الگوی :templateCode یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessException',
        'notification body could not process for template code : :templateCode');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessException',
        'محتوای نوتیفیکشین با کد :templateCode الگوی قابل پردازش نمی باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessorDoesNotExistsException',
        'does not exists any body processor');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessorDoesNotExistsException',
        'پردازشگر محتوایی برای نوتیفکشین مورد نظر یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotificationTemplateNotFoundException',
        'no notification template found for : :requestedTemplateCode');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.notification.client.exception.NotificationTemplateNotFoundException',
        'الگو :requestedTemplateCode نوتیفیکشین یافت نگردید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.uaa.client.ClientAuthenticationException',
        'error on authentication');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.uaa.client.ClientAuthenticationException',
        'خطا در احراز هویت');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.ServiceEndpointPrepareException',
        'error on extract endpoint uri with metadata');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.ServiceEndpointPrepareException',
        'خطا در بازگشایی فراداده آدرس مقصد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotDefinedException',
        'java service class not found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotDefinedException',
        'کلاس جاوایی سرویس مورد نظر پیدا نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotFoundException',
        'error load java service class');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotFoundException',
        'بارگزاری سرویس جاوا با مشکل مواجه گردید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceMethodNotFoundException',
        'method not found for java service');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceMethodNotFoundException',
        'متد جاوا در سرویس مورد نظر یافت نگردید ');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceParameterClassNotFoundException',
        'service [:serviceCode] , parameter [:parameterName] class not found.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.JavaServiceParameterClassNotFoundException',
        'پارامتر [:parameterName] در سرویس جاوا [:serviceCode] یافت نگردید.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderUnknownException',
        'provider :providerCode is unreachable');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderUnknownException',
        'سرور مقصد :providerCode در دسترس نمی باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderTimeoutException',
        'provider :providerCode for service :serviceCode is timed out');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderTimeoutException',
        'خطا در زمان پاسخ دهی خدمت :serviceCode از سرور مقصد :providerCode  به پایان رسیده است.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderUnreachableException',
        'provider :providerCode for service :serviceCode is unreachable');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderUnreachableException',
        'سرویس با کد :serviceCode  قابل فراخوانی در سرور مقصد با کد :providerCode نمی باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'ex::ir.daneshrefah.scm.plugin.api.exception.InvalidProviderResponseException',
        'invalid provider :providerCode response');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'ex::ir.daneshrefah.scm.plugin.api.exception.InvalidProviderResponseException',
        'خطا در پاسخ دریافتی از سمت سامانه :providerCode ');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.common.exception.ServiceNotFoundException',
        'no serviceCode[:serviceCode] found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.common.exception.ServiceNotFoundException',
        'سرویسی با کد [:serviceCode] یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'exo::org.springframework.dao.DataIntegrityViolationException::SQLCODE=-803',
        'duplicated record');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'exo::org.springframework.dao.DataIntegrityViolationException::SQLCODE=-803',
        'رکورد مورد نظر تکراری می باشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US',
        'exo::org.springframework.dao.DataIntegrityViolationException::integrity',
        'integrity error');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR',
        'exo::org.springframework.dao.DataIntegrityViolationException::integrity',
        'خطا در صحت داده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::org.springframework.dao.DataIntegrityViolationException',
        'data integrity violation error');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::org.springframework.dao.DataIntegrityViolationException',
        'خطا در نقض یکپارچگی داده ها');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::java.sql.SQLIntegrityConstraintViolationException',
        'constraint error');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::java.sql.SQLIntegrityConstraintViolationException',
        'خطا در قواعد جامعیتی پایگاه داده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException',
        'invalid otp code.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException',
        'رمز یکبار مصرف اشتباه می باشد');


INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.uaa.exception.OtpAlreadyExistException',
        'otp already exist.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.uaa.exception.OtpAlreadyExistException',
        'رمز یکبار مصرف قبلا استفاده گردیده است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.uaa.exception.OtpCodeGenerationException',
        'error on create otp code.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.uaa.exception.OtpCodeGenerationException',
        'ایجاد رمز یکبار مصرف با مشکل مواجه گشت');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::ir.daneshrefah.scm.uaa.exception.OtpNotFoundException',
        'otp not found.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::ir.daneshrefah.scm.uaa.exception.OtpNotFoundException',
        'رمز یکبار مصرف یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::java.net.UnknownHostException',
        'unknown host.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::java.net.UnknownHostException',
        'خطا در شناسایی آدرس مقصد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::org.apache.camel.http.base.HttpOperationFailedException',
        'Http Operation Failed Exception.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::org.apache.camel.http.base.HttpOperationFailedException',
        'خطا در عملیات فراخوانی سرویس');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::java.net.NoRouteToHostException',
        'no route to host.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::java.net.NoRouteToHostException',
        'مسیریابی درخواست به سرور مقصد با مشکل مواجه گشت');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'en-US', 'ex::java.net.SocketTimeoutException',
        'no response was received from the provider');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', '2024-06-02 12:45:49.000000',
        '2024-06-02 12:45:43.000000', 'fa-IR', 'ex::java.net.SocketTimeoutException',
        'پاسخی از سمت سرور مقصد دریافت نگردید.');


INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US',
        'ex::ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundException',
        'no process instance found with ID = :processInstanceId');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundException',
        'فرایندی با شناسه :processInstanceId یافت نشد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US',
        'ex::ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundWithKeyException',
        'No process instance found with key = :processKey');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundWithKeyException',
        'فرایندی با شناسه :processKey یافت نشد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.common.UnauthorizedException',
        'Unauthorized to cancel the process');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.common.UnauthorizedException',
        'شما مجاز به لغو این فرآیند نمی باشید.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.common.PersonNotFoundException',
        'Person not found with id: :id');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.common.PersonNotFoundException',
        'کاربری با این شناسه :id یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.task.TaskNotFoundException',
        'Task not found');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.task.TaskNotFoundException',
        'تسکی یافت نشد');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.attachment.AttachmentPermissionException',
        'You do not have permission to :operation this attachment = :attachment');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.attachment.AttachmentPermissionException',
        'شما اجازه‌ :operation پیوست :attachment را ندارید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.task.UnauthorizedCompleteTaskException',
        'Unauthorized to complete the task');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.task.UnauthorizedCompleteTaskException',
        'شما مجاز به تکمیل این تسک نمی باشید.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.task.exception.InvalidPasswordException',
        'Invalid password.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.task.exception.InvalidPasswordException',
        'رمز وارد شده نادرست است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.task.exception.ProcessAuthorityException',
        'have not permission');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.task.exception.ProcessAuthorityException',
        'دسترسی ندارید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'process.state.active',
        'ACTIVE');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'process.state.active',
        'فعال');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'process.state.suspended',
        'SUSPENDED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'process.state.suspended',
        'معلق');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'process.state.completed',
        'COMPLETED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'process.state.completed',
        'کامل شده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'process.state.externally_terminated',
        'EXTERNALLY_TERMINATED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'process.state.externally_terminated',
        'خاتمه یافته خارجی');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'process.state.internally_terminated',
        'INTERNALLY_TERMINATED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'process.state.internally_terminated',
        'خاتمه یافته داخلی');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'process.state.canceled',
        'CANCELED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'process.state.canceled',
        'انصراف');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'task.state.completed',
        'COMPLETED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'task.state.completed',
        'کامل شده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'task.state.deleted',
        'DELETED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'task.state.deleted',
        'حذف');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'task.state.canceled',
        'CANCELED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'task.state.canceled',
        'انصراف');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'task.state.expired',
        'EXPIRED');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'task.state.expired',
        'منقضی');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'task.state.error',
        'ERROR');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'task.state.error',
        'خطا');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'task.state.migration',
        'MIGRATION');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'task.state.migration',
        'مهاجرت');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.definition.ProcessDefinitionExistsException',
        'Cannot delete process definitions because process exist for deployment id = :deploymentId');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.definition.ProcessDefinitionExistsException',
        'شما مجاز به حذف نمی باشید زیرا فرایندهایی برای این شناسه استقرار :deploymentId وجود دارد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'Stamp Cancel Task',
        'Payment Transfer');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'Stamp Cancel Task',
        'انتفال وجه');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'Waiting for accepting or rejecting all users',
        'Payment Transfer');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'Waiting for accepting or rejecting all users',
        'انتفال وجه');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'Subscription Payment Transfer',
        'Payment Transfer');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'Subscription Payment Transfer',
        'انتفال وجه');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'PENDING',
        'درانتظار اقدام');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'COMPLETE',
        'کامل‌ شده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'CANCEL',
        'لغو شده');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'WAITING_FOR_CONFIRM',
        'در انتظار‌ تایید');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'WAITING_FOR_ACKNOWLEDGE',
        'در انتظار‌ پاسخ');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR', 'FAIL',
        'خطا');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'en-US', 'ex::ir.daneshrefah.scm.process.exception.task.InvalidAssigneeException',
        'Invalid assignee: Either the assignee is blank or does not match for a non-admin user.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.process.exception.task.InvalidAssigneeException',
        'شناسه کاربر نامعتبر:شناسه کاربر خالی است یا برای کاربر غیرمدیر مطابقت ندارد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', (CURRENT DATE),
        (CURRENT DATE), 'fa-IR',
        'ex::ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException',
        'فراخوانی سرویس مقصد در حال حاضر امکان پذیر نمی باشد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Dariush Abdolahi', 'Dariush Abdolahi', (CURRENT DATE),
        (CURRENT DATE), 'en-US',
        'ex::ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException',
        'It is currently not possible to call the destination service.');


INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', CURRENT DATE,
        CURRENT DATE, 'en-US',
        'ex::ir.daneshrefah.scm.task.exception.ProcessInstanceCompleteException',
        'Cannot update description of a completed process instance.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', CURRENT DATE,
        CURRENT DATE, 'fa-IR',
        'ex::ir.daneshrefah.scm.task.exception.ProcessInstanceCompleteException',
        'نمی‌توان فرآیند تکمیل شده را به‌روزرسانی کرد.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', CURRENT DATE,
        CURRENT DATE, 'en-US',
        'ex::ir.daneshrefah.scm.task.exception.InvalidTaskStatusException',
        'Invalid task status.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', CURRENT DATE,
        CURRENT DATE, 'fa-IR',
        'ex::ir.daneshrefah.scm.task.exception.InvalidTaskStatusException',
        'وضعیت تسک نامعتبر است');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', CURRENT DATE,
        CURRENT DATE, 'en-US',
        'ex::ir.daneshrefah.scm.task.exception.InvalidProcessStatusException',
        'Invalid process status.');

INSERT INTO REF.TBL_SCM_RESOURCE_BUNDLE (CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, LOCALE_CODE, KEY, VALUE)
VALUES ('Alireza Rayani', 'Alireza Rayani', CURRENT DATE,
        CURRENT DATE, 'fa-IR',
        'ex::ir.daneshrefah.scm.task.exception.InvalidProcessStatusException',
        'وضعیت فرآیند نامعتبر است');

CREATE TABLE REF.TBL_SCM_ERROR_MAPPING
(
    ERROR_MAPPING_ID             DECIMAL(22) GENERATED ALWAYS AS IDENTITY NOT NULL,
    EXTERNAL_SERVICE_PROVIDER_ID VARCHAR(36),
    PROVIDER_ERROR_CODE          VARCHAR(36),
    EXCEPTION_CLASS_NAME         VARCHAR(300),
    EXCEPTION_OVERRIDE_NAME      VARCHAR(300),
    SCM_ERROR_CODE               DECIMAL(10)                              NOT NULL,
    STATUS_CODE                  VARCHAR(50)                              NOT NULL,
    CREATE_DATE                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                      VARCHAR(255),
    LAST_EDITOR                  VARCHAR(255),
    FOREIGN KEY (EXTERNAL_SERVICE_PROVIDER_ID) REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID),
    PRIMARY KEY (ERROR_MAPPING_ID)
);
INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('', 2001, 'sc_ebz', 'Reza Jamshidi', 'Reza Jamshidi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.TerminalServiceNotFoundException',
        1023, 'sc_nfd',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.TooManyRecordFoundException',
        1010, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException',
        1018, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.NoDataChangedException',
        1021, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.NoCustomerFoundException',
        1104, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.NoAssetFoundException',
        1105, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.MissingRequiredInputException',
        1009, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.MissingRequestException',
        1014, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.MethodNotSupportedException',
        1019, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.MethodNotSupportDataException',
        1019, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.InvalidRequestFormatException',
        1020, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.InvalidRemoteResponseException',
        1012, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.InvalidInputException',
        1007, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.InvalidInputDateFormatException',
        1008, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.InputMismatchException',
        1031, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.InputAlreadyExistException',
        1006, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.DuplicatedRecordFoundException',
        1029, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.AuthenticationRequiredException',
        1002, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.TerminalNotAssignedServiceException',
        1023, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');


INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.ServiceInvalidMetadataException',
        1039, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.RecordVersionException',
        1205, 'sc_ebz',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.MessagePayloadMergeException',
        1037, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.MessagePayloadMergeException',
        1237, 'sc_acd',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.DisableServiceExecutionException',
        1022, 'sc_nfd',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.PersonNotFoundException',
        1036, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');


INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.TransformException',
        1026, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.notification.client.exception.EmptyNotificationRequestException',
        1150, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.notification.client.exception.InvalidNotificationRequestException',
        1151, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.notification.client.exception.NotFoundSupportedBodyProcessorException',
        1152, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessException',
        1153, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.notification.client.exception.NotificationBodyProcessorDoesNotExistsException',
        1154, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.notification.client.exception.NotificationTemplateNotFoundException',
        1155, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.uaa.client.ClientAuthenticationException',
        1160, 'sc_acd',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.ServiceEndpointPrepareException',
        1138, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotDefinedException',
        1033, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.JavaServiceClassNotFoundException',
        1032, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.JavaServiceMethodNotFoundException',
        1027, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.JavaServiceParameterClassNotFoundException',
        1034, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.ProviderUnknownException',
        1025, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.ProviderTimeoutException',
        1037, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.ProviderUnreachableException',
        1017, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.plugin.api.exception.InvalidProviderResponseException',
        1012, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.common.exception.ServiceNotFoundException',
        1024, 'sc_nfd',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('java.lang.Exception',
        1001, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, EXCEPTION_OVERRIDE_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('org.springframework.dao.DataIntegrityViolationException',
        'SQLCODE=-803',
        1006, 'sc_div',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, EXCEPTION_OVERRIDE_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('org.springframework.dao.DataIntegrityViolationException',
        'integrity',
        1028, 'sc_div',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('org.springframework.dao.DataIntegrityViolationException',
        1028, 'sc_div',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('java.sql.SQLIntegrityConstraintViolationException',
        1028, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.uaa.exception.InvalidOtpCodeException',
        1250, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.uaa.exception.OtpAlreadyExistException',
        1250, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.uaa.exception.OtpCodeGenerationException',
        1250, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('ir.daneshrefah.scm.uaa.exception.OtpNotFoundException',
        1250, 'sc_evl',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('java.net.UnknownHostException',
        1016, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('org.apache.camel.http.base.HttpOperationFailedException',
        1012, 'sc_esy',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('java.net.NoRouteToHostException',
        1017, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES ('java.net.SocketTimeoutException',
        1017, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundException',
        1200, 'sc_nfd',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundWithKeyException',
        1201, 'sc_nfd',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.common.UnauthorizedException',
        1202, 'sc_uat',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.common.PersonNotFoundException',
        1202, 'sc_nfd',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.attachment.AttachmentPermissionException',
        1204, 'sc_acd',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.task.TaskNotFoundException',
        1205, 'sc_nfd',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.task.UnauthorizedCompleteTaskException',
        1206, 'sc_uat',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.schema.JsonSchemaException',
        1207, 'sc_evl',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.definition.ProcessDefinitionExistsException',
        1208, 'sc_evl',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.process.exception.task.InvalidAssigneeException',
        1209, 'sc_uat',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.common.exception.RestExternalServiceProviderException',
        1300, 'sc_eup',
        'Dariush Abdolahi', 'Dariush Abdolahi');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.task.exception.ProcessInstanceCompleteException',
        1304, 'sc_evl',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.task.exception.InvalidTaskStatusException',
        1305, 'sc_evl',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.task.exception.InvalidProcessStatusException',
        1306, 'sc_evl',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.task.exception.ProcessAuthorityException',
        1308, 'sc_acd',
        'Alireza Rayani', 'Alireza Rayani');

INSERT INTO "REF".TBL_SCM_ERROR_MAPPING
(EXCEPTION_CLASS_NAME, SCM_ERROR_CODE, STATUS_CODE, CREATOR, LAST_EDITOR)
VALUES( 'ir.daneshrefah.scm.task.exception.InvalidPasswordException',
        1309, 'sc_evl',
        'Alireza Rayani', 'Alireza Rayani');


-- finalized tables
-------------------------------------------------------------------
CREATE TABLE REF.TBL_SCM_AUTHORITY
(
    AUTHORITY_ID                       VARCHAR(36) NOT NULL,
    AUTHORITY_TYPE_CODE                SMALLINT    NOT NULL,
    SOURCE_TERMINAL_ID                 VARCHAR(36) NOT NULL,
    SOURCE_CHANNEL_ID                  VARCHAR(36),
    SOURCE_SERVICE_ID                  VARCHAR(36),
    SOURCE_AUTHENTICATION_METHOD_CODE  VARCHAR(36),
    SOURCE_CONDITION                   VARCHAR(500),
    SOURCE_USER_ID                     VARCHAR(36),
    SOURCE_MEMBERSHIP_ID               VARCHAR(36),
    TARGET_SERVICE_ACCESS_ALLOW        SMALLINT  DEFAULT 1,
    TARGET_WITHDRAW_DURATION_TYPE_CODE SMALLINT,
    TARGET_WITHDRAW_DURATION           SMALLINT,
    TARGET_WITHDRAW_MIN_AMOUNT         DECIMAL(20, 2),
    TARGET_WITHDRAW_MAX_AMOUNT         DECIMAL(20, 2),
    CREATE_DATE                        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE                     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                            VARCHAR(255),
    LAST_EDITOR                        VARCHAR(255),
    FOREIGN KEY (SOURCE_TERMINAL_ID) REFERENCES REF.TBL_SCM_TERMINAL (TERMINAL_ID),
    FOREIGN KEY (SOURCE_CHANNEL_ID) REFERENCES REF.TBL_SCM_CHANNEL (CHANNEL_ID),
    FOREIGN KEY (SOURCE_SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID),
    PRIMARY KEY (AUTHORITY_ID)
);
INSERT INTO "REF".TBL_SCM_AUTHORITY
(AUTHORITY_ID, AUTHORITY_TYPE_CODE, SOURCE_TERMINAL_ID, SOURCE_CHANNEL_ID, SOURCE_SERVICE_ID,
 SOURCE_AUTHENTICATION_METHOD_CODE, SOURCE_CONDITION, SOURCE_USER_ID, SOURCE_MEMBERSHIP_ID,
 TARGET_WITHDRAW_DURATION_TYPE_CODE, TARGET_WITHDRAW_DURATION, TARGET_WITHDRAW_MIN_AMOUNT, TARGET_WITHDRAW_MAX_AMOUNT,
 CREATOR, LAST_EDITOR)
VALUES ('8d230ef3-4a9c-4a3a-a8e4-0d8e0a8712c7', 2, 'a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', null, null, null, null, null,
        null, 1, 1, 0, 1000000000, 'Reza Jamshidi', 'Reza Jamshidi');


INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('8f71b0f1-4b53-4c81-a40d-88e6cc53a3a7', 'ایجاد بدنه خالی', 'bean:emptyTransformer', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('d6b1a874-2c7c-4932-a6fc-9a7e1d038b03', 'پویا', 'bean:dynamicTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('675e0d37-af60-4f0c-88e7-5a499b20f3ac', 'ریکوئست استعلام شبا',
        'ir.daneshrefah.scm.plugin.nab.transformer.IbanInqRequestTransformer', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('1f8e84e3-56a3-47f2-ba85-c2a285430824', 'ریسپانس استعلام شبا', 'bean:ibanInqResponseTransformer',
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('c7943a8d-0db1-4851-951e-1d4a89439f5f', 'افزودن ویژگی', 'bean:appendPropTransformer', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('b78fe97e-8e60-45c1-a235-3a0c7f0a6ecf', 'Nab Request Transformer', 'bean:nabRequestTransformer',
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('fd0a7f8f-af2d-4c5b-aa0f-55e9f58b41ab', 'Nab Response Transformer', 'bean:nabResponseTransformer',
        'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER
    (TRANSFORMER_ID, TITLE, JAVA_CLASS_NAME, CREATOR, LAST_EDITOR)
VALUES ('a0e4b0c6-9c8a-46c9-aaec-38d3d486b3c3', 'Append LoggedIn User Info', 'bean:appendLoggedInInfo', 'Reza Jamshidi',
        'Reza Jamshidi');



INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (1, 1, '675e0d37-af60-4f0c-88e7-5a499b20f3ac', '864d62c0-725c-4da4-91e6-8627fc0971c8', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (2, 1, '1f8e84e3-56a3-47f2-ba85-c2a285430824', '864d62c0-725c-4da4-91e6-8627fc0971c8', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (3, 1, '8f71b0f1-4b53-4c81-a40d-88e6cc53a3a7', '7d2e1a3b-e894-4ea3-b01a-685361fd75e5', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (4, 1, '[{"sourceProp": "body", "targetProp": "iban"}]', 'c7943a8d-0db1-4851-951e-1d4a89439f5f',
        '7d2e1a3b-e894-4ea3-b01a-685361fd75e5', 'Reza Jamshidi', 'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (1, 1, null, 'b78fe97e-8e60-45c1-a235-3a0c7f0a6ecf', 'ebb0b663-8ee2-4b22-9f1a-3d9f985d6a41', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (2, 1, null, 'fd0a7f8f-af2d-4c5b-aa0f-55e9f58b41ab', 'ebb0b663-8ee2-4b22-9f1a-3d9f985d6a41', 'Reza Jamshidi',
        'Reza Jamshidi');
INSERT INTO "REF".TBL_SCM_TRANSFORMER_RELATION
(RELATION_TYPE_CODE, "ORDER", "METADATA", TRANSFORMER_ID, SOURCE_ID, CREATOR, LAST_EDITOR)
VALUES (11, 1, null, 'a0e4b0c6-9c8a-46c9-aaec-38d3d486b3c3', '6d55a24b-63ef-4e25-b376-9d43f4e155cc', 'Reza Jamshidi',
        'Reza Jamshidi');
------------------------------------------------------------------------------------------------------------------------

CREATE TABLE REF.TBL_SCM_PARAMETERS
(
    PARAMETER_ID               DECIMAL(22) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    NAME                       VARCHAR(255),
    TYPE                       VARCHAR(255),
    INTERNAL                   SMALLINT                                NOT NULL,
    TAG                        VARCHAR(255),
    REQUIRED                   SMALLINT                                NOT NULL,
    ORDER                      INTEGER,
    PARENT_ID                  DECIMAL(22),
    ACTION_TYPE                INTEGER,
    DATA_SOURCE_PROPERTY       INTEGER,
    DATA_SOURCE_VALUE          VARCHAR(255),
    DATA_SOURCE_LENGTH         INTEGER,
    DATA_SOURCE_CONVERTOR_CODE VARCHAR(255),
    DEFAULT_VALUE              VARCHAR(255),
    CREATOR                    VARCHAR(255),
    LAST_EDITOR                VARCHAR(255),
    CREATE_DATE                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_TBL_SCM_PARAMETERS PRIMARY KEY (PARAMETER_ID)
);

ALTER TABLE REF.TBL_SCM_PARAMETERS
    ADD CONSTRAINT FK_TBL_SCM_PARAMETERS_ON_PARENT FOREIGN KEY (PARENT_ID) REFERENCES REF.TBL_SCM_PARAMETERS (PARAMETER_ID);

CREATE TABLE REF.TBL_SCM_PARAMETER_EXTERNAL_SERVICE_RELATION
(
    PARAMETER_ID   DECIMAL(22),
    SERVICE_ID     VARCHAR(36)
);
ALTER TABLE REF.TBL_SCM_PARAMETER_SERVICE_RELATION ADD CONSTRAINT FK_ON_PARAMETER_ID
    FOREIGN KEY (SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID);
ALTER TABLE REF.TBL_SCM_PARAMETER_SERVICE_RELATION ADD CONSTRAINT FK_ON_SERVICE_ID
    FOREIGN KEY (PARAMETER_ID) REFERENCES REF.TBL_SCM_PARAMETERS (PARAMETER_ID);

CREATE TABLE REF.TBL_SCM_DATASOURCE_CONDITION
(
    ID                         DECIMAL(22) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    RESPONSE_CONDITION_ID      DECIMAL(22),
    CONDITION_VALUE            VARCHAR(255),
    DATA_SOURCE_PROPERTY       INTEGER,
    DATA_SOURCE_VALUE          VARCHAR(255),
    DATA_SOURCE_LENGTH         INTEGER,
    DATA_SOURCE_CONVERTOR_CODE VARCHAR(255),
    OPERATION                  INTEGER,
    CREATOR                    VARCHAR(255),
    LAST_EDITOR                VARCHAR(255),
    CREATE_DATE                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_TBL_SCM_DATASOURCE_CONDITION PRIMARY KEY (ID)
);

CREATE TABLE REF.TBL_SCM_SERVICE_RESPONSE_CONDITION
(
    ID                      DECIMAL(22) GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    RESPONSE_TRANSFORMER_ID VARCHAR(255),
    SERVICE_ID              VARCHAR(36),
    SERVICE_PROVIDER_ID     VARCHAR(36),
    RESP_ERROR_CODE         VARCHAR(255),
    RESP_ERROR_MESSAGE      VARCHAR(255),
    RESPONSE_BODY_TYPE      VARCHAR(64) NOT NULL ,
    CREATOR                 VARCHAR(255),
    LAST_EDITOR             VARCHAR(255),
    CREATE_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT PK_TBL_SCM_SERVICE_RESPONSE_CONDITION PRIMARY KEY (ID)
);

ALTER TABLE REF.TBL_SCM_SERVICE_RESPONSE_CONDITION
    ADD CONSTRAINT FK_TBL_SCM_SERVICE_RESPONSE_CONDITION_ON_RESPONSE_TRANSFORMER FOREIGN KEY (RESP_TRANSFORMER_ID) REFERENCES TBL_SCM_TRANSFORMER (TRANSFORMER_ID);

ALTER TABLE REF.TBL_SCM_SERVICE_RESPONSE_CONDITION
    ADD CONSTRAINT FK_TBL_SCM_EXTERNAL_SERVICE_RESPONSE_CONDITION_ON_SERVICE FOREIGN KEY (SERVICE_ID) REFERENCES REF.TBL_SCM_SERVICE (SERVICE_ID);

ALTER TABLE REF.TBL_SCM_SERVICE_RESPONSE_CONDITION
    ADD CONSTRAINT FK_TBL_SCM_ON_SERVICE_PROVIDER FOREIGN KEY (SERVICE_PROVIDER_ID) REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID);

CREATE TABLE REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION
(
    PARAMETER_ID            DECIMAL(22),
    RESPONSE_CONDITION_ID   DECIMAL(22)
);

ALTER TABLE REF.TBL_SCM_PARAMETER_RESPoNSE_CONDITION_RELATION ADD CONSTRAINT FK_ON_PARAMETER_ID
    FOREIGN KEY (RESPONSE_CONDITION_ID) REFERENCES REF.TBL_SCM_SERVICE_RESPONSE_CONDITION (ID);
ALTER TABLE REF.TBL_SCM_PARAMETER_RESPoNSE_CONDITION_RELATION ADD CONSTRAINT FK_ON_SERVICE_ID
    FOREIGN KEY (PARAMETER_ID) REFERENCES REF.TBL_SCM_PARAMETERS (PARAMETER_ID);

CREATE TABLE REF.TBL_SCM_PARAMETER_SERVICE_PROVIDER_RELATION
(
    PARAMETER_ID   DECIMAL(22),
    SERVICE_PROVIDER_ID     VARCHAR(36)
);

ALTER TABLE REF.TBL_SCM_PARAMETER_SERVICE_PROVIDER_RELATION ADD CONSTRAINT FK_ON_PARAMETER_ID
    FOREIGN KEY (SERVICE_PROVIDER_ID) REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID);
ALTER TABLE REF.TBL_SCM_PARAMETER_SERVICE_PROVIDER_RELATION ADD CONSTRAINT FK_ON_SERVICE_ID
    FOREIGN KEY (PARAMETER_ID) REFERENCES REF.TBL_SCM_PARAMETERS (PARAMETER_ID);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('requestID', 'STRING', 0, null, 1, null, 8, 1, 60, '', null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:39:43.528322', '2024-07-31 06:39:43.528322', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('encoding', 'STRING', 0, null, 1, null, 8, 1, 14, 'ASCII', 5, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:41:21.638612', '2024-07-31 06:41:21.638612', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('callType', 'STRING', 0, null, 1, null, 8, 1, 14, 'Reader', 6, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:42:39.050353', '2024-07-31 06:42:39.050353', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('parameters', 'ARRAY', 0, null, 1, null, 8, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:45:00.181690', '2024-07-31 06:45:00.181690', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 7, 1, 14, 'P_ENDROW', 8, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:49:31.815539', '2024-07-31 06:49:31.815539', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 7, 1, 14, '111', 3, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:49:31.815539', '2024-07-31 06:49:31.815539', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 06:51:33.263078', '2024-07-31 06:51:33.263078', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, null, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:05:02.498402', '2024-07-31 07:05:02.498402', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 11, 1, 14, 'P_STARTROW', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:06:59.601268', '2024-07-31 07:06:59.601268', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 11, 1, 14, '1', 1, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:08:37.371400', '2024-07-31 07:08:37.371400', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:09:39.592809', '2024-07-31 07:09:39.592809', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:18:30.050596', '2024-07-31 07:18:30.050596', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 12, 1, 14, 'P_ACCOUNTSTATUS', 15, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:20:20.551446', '2024-07-31 07:20:20.551446', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 12, 1, 14, '-1', 2, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:20:20.564362', '2024-07-31 07:20:20.564362', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:21:16.312845', '2024-07-31 07:21:16.312845', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 15, 1, 14, 'P_SIGNER', 8, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:23:14.043111', '2024-07-31 07:23:14.043111', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 15, 1, 14, '-1', 2, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:23:14.052198', '2024-07-31 07:23:14.052198', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:24:10.448362', '2024-07-31 07:24:10.448362', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 18, 1, 14, 'P_CUSTOMERID', 12, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:30:32.625201', '2024-07-31 07:30:32.625201', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 18, 1, 10, 'customerNo', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:30:32.634637', '2024-07-31 07:30:32.634637', '0');

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-07-31 07:09:39.592809', '2024-07-31 07:09:39.592809', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 21, 1, 14, 'P_NATIONALID', 12, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-03 10:10:16.590103', '2024-08-03 10:10:16.590103', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 21, 1, 10, 'nationalId', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-03 10:10:16.603928', '2024-08-03 10:10:16.603928', '0');

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 4, 1, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-03 10:11:20.590625', '2024-08-03 10:11:20.590625', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('name', 'STRING', 0, null, 1, null, 24, 1, 14, 'P_SUBORGAN', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-03 10:10:16.590103', '2024-08-03 10:10:16.590103', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('value', 'STRING', 0, null, 1, null, 24, 1, 10, 'subOrg', 6, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-03 10:10:16.603928', '2024-08-03 10:10:16.603928', '0');

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('Content-Type', 'STRING', 0, null, 1, null, null, 2, 14, 'application/json', 16, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-03 13:24:08.119525', '2024-08-03 13:24:08.119525', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, null, 5, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 08:32:49.236665', '2024-08-04 08:32:49.236665', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('mainOwner', 'STRING', 0, null, 1, null, 31, 5, 10, 'MAINOWNER', 9, '', 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 08:34:33.318778', '2024-08-04 08:34:33.318778', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'ARRAY', 0, null, 1, null, 28, 5, 10, 'result', 6, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 08:36:14.702115', '2024-08-04 08:36:14.702115', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES (null, 'OBJECT', 0, null, 1, null, 30, 5, 14, null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 08:37:33.008515', '2024-08-04 08:37:33.008515', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountOwnerCustomerNo', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'OWNERID', 7, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:40:48.709611', '2024-08-04 09:40:48.709611', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountOwnerName', 'STRING', 0, null, 1, null, 31, 5, 10, 'OWNERNAME', 9, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:41:21.869554', '2024-08-04 09:41:21.869554', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('ownerNationalId', 'STRING', 0, null, 1, null, 31, 5, 10, 'NATIONALID', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:41:41.733333', '2024-08-04 09:41:41.733333', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountOwnerCustomerTypeCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'CUSTOMERTYPE', 12, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:42:01.446183', '2024-08-04 09:42:01.446183', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountOwnerCustomerTypeTitle', 'STRING', 0, null, 1, null, 31, 5, 10, 'CUSTOMERTITILE', 14, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:42:25.585847', '2024-08-04 09:42:25.585847', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('generalCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'GENERAL', 7, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:42:49.357718', '2024-08-04 09:42:49.357718', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('subsidryCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'SUBSIDRY', 8, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:43:12.021038', '2024-08-04 09:43:12.021038', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('subsidryTitle', 'STRING', 0, null, 1, null, 31, 5, 10, 'SUBSIDRYTITLE', 13, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:43:33.650170', '2024-08-04 09:43:33.650170', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountTypeCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'ACCOUNTTYPE', 11, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:44:26.980531', '2024-08-04 09:44:26.980531', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountTypeTitle', 'STRING', 0, null, 1, null, 31, 5, 10, 'ACCOUNTTYPETITLE', 16, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:44:49.145125', '2024-08-04 09:44:49.145125', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountNumber', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'ACCOUNTNUMBER', 13, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:45:07.275478', '2024-08-04 09:45:07.275478', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountStatusCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'ACCOUNTSTATUS', 13, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:45:33.436368', '2024-08-04 09:45:33.436368', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountStatusTitle', 'STRING', 0, null, 1, null, 31, 5, 10, 'ACCOUNTSTATUSTITLE', 18, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:46:36.170118', '2024-08-04 09:46:36.170118', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('branchCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'BRANCHCODE', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:46:57.267556', '2024-08-04 09:46:57.267556', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('branchTitle', 'STRING', 0, null, 1, null, 31, 5, 10, 'BRANCHTITILE', 12, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:47:19.043007', '2024-08-04 09:47:19.043007', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountOpenDate', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'OPENACCOUNTDATE', 15, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:47:52.785089', '2024-08-04 09:47:52.785089', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('accountIsCommercial', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'ISCOMMERCE', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:48:16.875106', '2024-08-04 09:48:16.875106', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('currency', 'STRING', 0, null, 1, null, 31, 5, 10, 'ACCOUNTCURRENCY', 15, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:48:37.127339', '2024-08-04 09:48:37.127339', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('iban', 'STRING', 0, null, 1, null, 31, 5, 10, 'IBAN', 4, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:48:55.877089', '2024-08-04 09:48:55.877089', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('sayahCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'SAYAHCODE', 9, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:49:23.899584', '2024-08-04 09:49:23.899584', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('customerRelationTypeCode', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'RELATIONTYPECODE', 16, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:49:45.605175', '2024-08-04 09:49:45.605175', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('customerRelationTypeTitle', 'STRING', 0, null, 1, null, 31, 5, 10, 'RELATIONTYPETITLE', 16, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:50:04.321166', '2024-08-04 09:50:04.321166', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('customerIsSigner', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'ISSIGNER', 8, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:51:24.235000', '2024-08-04 09:51:24.235000', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('customerSharePercent', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'SHAREPERCENT', 12, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:51:51.572662', '2024-08-04 09:51:51.572662', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('balanceTotal', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'BALANCETOTAL', 12, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:52:13.173299', '2024-08-04 09:52:13.173299', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('balanceAvailable', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'BALANCEAVAILABLE', 16, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:52:42.069974', '2024-08-04 09:52:42.069974', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('blockTotal', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'BLOCKTOTAL', 10, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:53:11.288737', '2024-08-04 09:53:11.288737', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('signDate', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'SIGNDATE', 8, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:54:33.965449', '2024-08-04 09:54:33.965449', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('lastTransactionDate', 'NUMBER', 0, null, 1, null, 31, 5, 10, 'LASTTRANSDATE', 13, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:55:09.931779', '2024-08-04 09:55:09.931779', null);

INSERT INTO REF.TBL_SCM_PARAMETERS (NAME, TYPE, INTERNAL, TAG, REQUIRED, ORDER, PARENT_ID, ACTION_TYPE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE, "DEFAULT_VALUE ")
VALUES ('subOrganization', 'STRING', 0, null, 1, null, 31, 5, 10, 'SUBORGAN', 8, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 09:55:30.583858', '2024-08-04 09:55:30.583858', null);

INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (28, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (29, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (30, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (31, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (32, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (33, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (34, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (35, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (36, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (37, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (38, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (39, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (40, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (41, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (42, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (43, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (44, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (45, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (46, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (47, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (48, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (49, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (50, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (51, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (52, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (53, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (54, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (55, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (56, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (57, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (58, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (59, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (60, 1);
INSERT INTO REF.TBL_SCM_PARAMETER_RESPONSE_CONDITION_RELATION (PARAMETER_ID, RESPONSE_CONDITION_ID) VALUES (61, 1);


INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (1, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (2, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (3, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (4, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (5, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (6, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (7, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (8, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (9, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (10, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (11, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (12, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (13, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (14, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (15, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (16, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (17, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (18, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (19, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (20, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (21, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (22, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (23, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (24, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (25, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (26, '41088034-ad79-4f6a-9e04-4f6a31374148');
INSERT INTO REF.TBL_SCM_PARAMETER_SERVICE_RELATION (PARAMETER_ID, SERVICE_ID)VALUES (27, '41088034-ad79-4f6a-9e04-4f6a31374148');

INSERT INTO REF.TBL_SCM_SERVICE_RESPONSE_CONDITION (RESP_TRANSFORMER_ID, SERVICE_ID, SERVICE_PROVIDER_ID, RESP_ERROR_CODE, RESP_ERROR_MESSAGE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE) VALUES (null, null, '3ce3e10e-c3cd-49c7-ae5c-330a81e882d7', '1017', 'ex::ir.daneshrefah.scm.plugin.api.exception.ProviderUnreachableException', 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-05 12:39:56.795443', '2024-08-05 12:39:56.795443');
INSERT INTO REF.TBL_SCM_SERVICE_RESPONSE_CONDITION (RESP_TRANSFORMER_ID, SERVICE_ID, SERVICE_PROVIDER_ID, RESP_ERROR_CODE, RESP_ERROR_MESSAGE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE) VALUES (null, '41088034-ad79-4f6a-9e04-4f6a31374148', null, null, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 10:05:24.376085', '2024-08-04 10:05:24.376085');

INSERT INTO REF.TBL_SCM_DATASOURCE_CONDITION (RESPONSE_CONDITION_ID, CONDITION_VALUE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE) VALUES (1, '200', 40, '', 6, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-04 10:08:24.533629', '2024-08-04 10:08:24.533629');
INSERT INTO REF.TBL_SCM_DATASOURCE_CONDITION (RESPONSE_CONDITION_ID, CONDITION_VALUE, DATA_SOURCE_PROPERTY, DATA_SOURCE_VALUE, DATA_SOURCE_LENGTH, DATA_SOURCE_CONVERTOR_CODE, CREATOR, LAST_EDITOR, CREATE_DATE, LAST_EDIT_DATE) VALUES (2, '503', 40, '', 6, null, 'Dariush Abdolahi', 'Dariush Abdolahi', '2024-08-05 12:44:34.653508', '2024-08-05 12:44:34.653508');


------------------------------------------------------------------------------------------------------------------------

-- 14020424
CREATE TABLE REF.TBL_SCM_PROFILE
(
    PROFILE_ID                    VARCHAR(36) NOT NULL,
    SERVICE_COMPONENT_PROVIDER_ID VARCHAR(36) NOT NULL,
    CREATE_DATE                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE                TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR                       VARCHAR(255),
    LAST_EDITOR                   VARCHAR(255),
    NAME                          VARCHAR(255),
    TITLE                         VARCHAR(255),
    PRIMARY KEY (PROFILE_ID)
);
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
    CONDITION_ID     DECIMAL(22)  NOT NULL GENERATED ALWAYS AS IDENTITY primary key,
    TITLE            VARCHAR(255) NOT NULL,
    DESC             VARCHAR(255),
    TYPE             SMALLINT     NOT NULL,
    VALUE            VARCHAR(255) NOT NULL,
    PERIOD_TYPE      SMALLINT,
    PERIOD_VALUE     SMALLINT,
    IGNORABLE        SMALLINT     NOT NULL DEFAULT 0,
    BYPASS_IGNORABLE SMALLINT     NOT NULL DEFAULT 0,
    CREATE_DATE      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    CREATOR          VARCHAR(255),
    LAST_EDITOR      VARCHAR(255)
);
LABEL
ON COLUMN CORPDATA.REF.TBL_SCM_CONDITION.TYPE IS '1: MaxAmount, 2: CallRate, 3: Authority';
-- LABEL ON COLUMN CORPDATA.REF.TBL_SCM_CONDITION.TYPE IS 'Reports to Dept.';
--/////////////////////////////////////////////////////////
--Conditioning terminal per auth and sec.auth method
CREATE TABLE REF.TBL_SCM_TERMINAL_CONDITION
(
    TERMINAL_CONDITION_ID                DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    TERMINAL_ID                          VARCHAR(36) NOT NULL,
    STATUS                               SMALLINT    NOT NULL,
    LOGIN_AUTHENTICATION_METHOD_ID       SMALLINT,
    TRANSACTION_AUTHENTICATION_METHOD_ID SMALLINT,
    CONDITION_ID                         DECIMAL(22) NOT NULL,
    CREATE_DATE                          TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE                       TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CREATOR                              VARCHAR(255),
    LAST_EDITOR                          VARCHAR(255),
    FOREIGN KEY (TERMINAL_ID) REFERENCES "REF".TBL_SCM_TERMINAL (TERMINAL_ID),
    FOREIGN KEY (LOGIN_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD (AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (TRANSACTION_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD (AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (CONDITION_ID) REFERENCES "REF".TBL_SCM_CONDITION (CONDITION_ID),
    PRIMARY KEY (TERMINAL_CONDITION_ID)
);
--/////////////////////////////////////////////////////////
--Conditioning service per auth and sec.auth method
CREATE TABLE REF.TBL_SCM_SERVICE_CONDITION
(
    SERVICE_CONDITION_ID                 DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    SERVICE_ID                           VARCHAR(36) NOT NULL,
    STATUS                               SMALLINT    NOT NULL,
    LOGIN_AUTHENTICATION_METHOD_ID       SMALLINT,
    TRANSACTION_AUTHENTICATION_METHOD_ID SMALLINT,
    CONDITION_ID                         DECIMAL(22) NOT NULL,
    CREATE_DATE                          TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE                       TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CREATOR                              VARCHAR(255),
    LAST_EDITOR                          VARCHAR(255),
    FOREIGN KEY (SERVICE_ID) REFERENCES "REF".TBL_SCM_SERVICE (SERVICE_ID),
    FOREIGN KEY (LOGIN_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD (AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (TRANSACTION_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD (AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (CONDITION_ID) REFERENCES "REF".TBL_SCM_CONDITION (CONDITION_ID),
    PRIMARY KEY (SERVICE_CONDITION_ID)
);
--/////////////////////////////////////////////////////////
--Conditioning the combination of terminal and service, per auth and sec.auth method
CREATE TABLE REF.TBL_SCM_TERMINAL_SERVICE_CONDITION
(
    TERMINAL_SERVICE_CONDITION_ID        DECIMAL(22) NOT NULL GENERATED ALWAYS AS IDENTITY,
    TERMINAL_SERVICE_ACCESS_ID           VARCHAR(36) NOT NULL,
    STATUS                               SMALLINT    NOT NULL,
    LOGIN_AUTHENTICATION_METHOD_ID       SMALLINT,
    TRANSACTION_AUTHENTICATION_METHOD_ID SMALLINT,
    CONDITION_ID                         DECIMAL(22) NOT NULL,
    CREATE_DATE                          TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    LAST_EDIT_DATE                       TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CREATOR                              VARCHAR(255),
    LAST_EDITOR                          VARCHAR(255),
    FOREIGN KEY (TERMINAL_SERVICE_ACCESS_ID) REFERENCES "REF".TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_SERVICE_ACCESS_ID),
    FOREIGN KEY (LOGIN_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD (AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (TRANSACTION_AUTHENTICATION_METHOD_ID) REFERENCES "REF".AUTHENTICATION_METHOD (AUTHENTICATION_METHOD_ID),
    FOREIGN KEY (CONDITION_ID) REFERENCES "REF".TBL_SCM_CONDITION (CONDITION_ID),
    PRIMARY KEY (TERMINAL_SERVICE_CONDITION_ID)
);
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_UAA_USER_GROUP
(
    --Definition
    USER_GROUP_ID   VARCHAR(36) NOT NULL,
    USER_GROUP_CODE VARCHAR(36) NOT NULL,
    --Attribute
    STATUS          SMALLINT,
    TITLE           VARCHAR(255),
    --Versioning
    CREATE_DATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR         VARCHAR(255),
    LAST_EDITOR     VARCHAR(255),
    --Relation
    PRIMARY KEY (USER_GROUP_ID)
);
--Done
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET
(
    --Definition
    ASSET_ID        VARCHAR(36)  NOT NULL, --TODO AutoIncrement
    PROVIDER_ID     VARCHAR(255) NOT NULL,
    ASSET_TYPE_CODE SMALLINT     NOT NULL,
    --Attribute
    --Versioning
    CREATE_DATE     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR         VARCHAR(255),
    LAST_EDITOR     VARCHAR(255),
    --Relation
    FOREIGN KEY (PROVIDER_ID) REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID),
    PRIMARY KEY (ASSET_ID)
);
--Done
--/////////////////////////////////////////////////////////
-- REF.CUSTOMER--Done
ALTER TABLE REF.CUSTOMER
    ADD COLUMN PROVIDER_ID VARCHAR(36) NOT NULL DEFAULT '3ce3e10e-c3cd-49c7-ae5c-330a81e882d7';
ALTER TABLE REF.CUSTOMER
    ADD FOREIGN KEY (PROVIDER_ID) REFERENCES REF.TBL_SCM_SERVICE_PROVIDER (SERVICE_PROVIDER_ID);
-- todo rename TBL_SCM_EXTERNAL_SERVICE_PROVIDER

--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_CUSTOMER_ASSET
(                                              --todo use membership & customerAccount & card tables
    --Definition
    CUSTOMER_ASSET_ID    VARCHAR(36)  NOT NULL,
    CUSTOMER_ID          INTEGER      NOT NULL,
    ASSET_ID             VARCHAR(255) NOT NULL,
    ASSET_ACCOUNT_REF_ID VARCHAR(255),--reference ID to asset's table, related to asset type (ASSET_TYPE_ID). ACC type for account, CRD type for card, ...
    ASSET_CARD__REF_ID   VARCHAR(255),--todo define foreign key for both account & card

    --Attribute
    RELATION_TYPE        SMALLINT     NOT NULL,--0, Owner, 1: Advocacy, 2: Delegation, 4:...
    --Versioning
    CREATE_DATE          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR              VARCHAR(255),
    LAST_EDITOR          VARCHAR(255),
    --Relation
    FOREIGN KEY (CUSTOMER_ID) REFERENCES REF.CUSTOMER (CUSTOMER_ID),
    FOREIGN KEY (ASSET_ID) REFERENCES REF.TBL_SCM_ASSET (ASSET_ID),
    PRIMARY KEY (CUSTOMER_ASSET_ID)
);
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET_ACCOUNT
(                                         -- todo use account table
    --Definition
    ASSET_ACCOUNT_ID VARCHAR(36) NOT NULL,
    --Attribute
    ACCOUNT_NO       VARCHAR(36) NOT NULL,
    SHEBA_NO         VARCHAR(36) NOT NULL,
    STATUS           SMALLINT    NOT NULL,--0: Block, 1: Active, 3: Suspend, 4: Closed
    PRODUCT_TYPE     SMALLINT,--Should define the product type
    ACCOUNT_TYPE     SMALLINT,--Should define the sup type of product --todo define foreignKey to AccountType
    TITLE            VARCHAR(36) NOT NULL,
    TYPE             SMALLINT    NOT NULL,--0: Individual, 1: Legal
    SHARED           BOOLEAN     NOT NULL,
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
);
--Done
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET_CARD
(                                       --todo use card table
    --Definition
    ASSET_CARD_ID  VARCHAR(36) NOT NULL,

    --Attribute
    CARD_NO        VARCHAR(36) NOT NULL,
    STATUS         SMALLINT    NOT NULL,--0: Block, 1: Active, 3: Suspend, 4: Closed
    PRODUCT_TYPE   SMALLINT,--Should define the product type
    CVV2           SMALLINT    NOT NULL,--Should define the sup type of product
    EXPIRE_DATE    TIMESTAMP   NOT NULL,
    MEDIA_TYPE     SMALLINT,--0: Magnet, 1: Smart, 2: eCard
    HOLDER         VARCHAR(36) NOT NULL,
    --Versioning
    CREATE_DATE    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LAST_EDIT_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CREATOR        VARCHAR(255),
    LAST_EDITOR    VARCHAR(255),
    --Relation
    PRIMARY KEY (ASSET_CARD_ID)
);
--Done
--/////////////////////////////////////////////////////////
CREATE TABLE REF.TBL_SCM_ASSET_CARD_ACCOUNT
( --todo use card table
    --Definition
    ASSET_CARD_ACCOUNT_ID VARCHAR(36) NOT NULL,
    ASSET_CARD_ID         VARCHAR(36) NOT NULL,
    ASSET_ACCOUNT_ID      VARCHAR(36) NOT NULL,
    --Attribute
    MAIN_ACCOUNT          BOOLEAN     NOT NULL,
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
);
--Done
--/////////////////////////////////////////////////////////
--Alter
--REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER--Done
ALTER TABLE REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER
    ADD COLUMN CUSTOMER_PROVIDE_METHOD_CODE SMALLINT NOT NULL;
ALTER TABLE REF.TBL_SCM_EXTERNAL_SERVICE_PROVIDER
    ADD COLUMN STATUS SMALLINT;

--/////////////////////////////////////////////////////////

--Audit log centralize table
CREATE TABLE REF.TBL_SCM_AUDIT_LOG
(
    ID            VARCHAR(36) NOT NULL,
    REVISION_TYPE INTEGER,
    TIMESTAMP     TIMESTAMP,
    CREATOR       VARCHAR(255),
    MODIFY_BY     VARCHAR(255),
    CLASS_TYPE    VARCHAR(255),
    TYPE_ID       VARCHAR(255),
    VALUE         CLOB,
    CONSTRAINT PK_TBL_SCM_AUDIT_LOG PRIMARY KEY (ID)
);