create table TBL_SUA_CLIENT
(

    USER_CHANNEL_AUTHENTICATION_ID INTEGER
        constraint TBL_SUA_CLIENT_UCA_ID_FK references REF.USER_CHANNEL_AUTHENTICATION,
    STATUS                         SMALLINT,
    LEGAL_USER_ID                  INTEGER
        constraint TBL_SUA_CLIENT_USER_USER_ID_FK references REF.USER
);



alter table TBL_SUA_CLIENT
    add USER_CHANNEL_AUTHENTICATION_ID INTEGER
        constraint TBL_SUA_CLIENT_UCA_ID_FK
            references REF.USER_CHANNEL_AUTHENTICATION;


alter table TBL_SUA_CLIENT
    add STATUS SMALLINT;


alter table TBL_SUA_CLIENT
    add LEGAL_USER_ID INTEGER
        constraint TBL_SUA_CLIENT_USER_USER_ID_FK
            references REF.USER;

alter table REF.TBL_SUA_CLIENT
    alter column SESSION_TTL_MINUTE set data type INTEGER;

-------------------------- INSERT DATA -------------------------------

INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('e71af055-8b5b-4b5c-89b0-d811c8facdcb', 'SVC_ASSETS_MCA_LIST', 'لیست دارایی های متصل به ترمینال کاربر',
        '/mca/list', 1, 1, 0, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 1, 0, null,
        null, null, '2025-04-29 16:31:29.438259', '2025-04-29 16:31:29.438259', 'SYSTEM', 'SYSTEM', null, null, null,
        null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('9f30c750-2aa1-4540-8ab9-87589cfdcd1e', 'SVC_MCSA_ASSIGNMENT', 'سرویس تخصیص یا سلب سرویس از به دارایی',
        '/mcsa/service-assignment', 1, 1, 0, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 6, 2, null, null, 1, 0,
        1, 0, null, null, null, '2025-05-04 16:48:33.238158', '2025-05-04 16:48:33.238158', 'SYSTEM', 'SYSTEM', null,
        null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('f9fcb242-91bc-4278-b7cb-39a8f1447e7e', 'SVC_ASSETS_MCA_GET', 'دریافت دارایی متصل به کانال با شناسه',
        '/mca/get/{id}', 1, 1, 0, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 3, 2, null, null, 1, 0, 1, 0,
        null, null, null, '2025-05-04 13:51:14.509268', '2025-05-04 13:51:14.509268', 'SYSTEM', 'SYSTEM', null, null,
        null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('ed84c749-a9c2-4b61-a0ff-4bbdcd7b41d9', 'SVC_ASSETS_MCA_EDIT', 'ویرایش دارایی متصل به کانال', '/mca/edit', 1, 1,
        0, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 6, 2, null, null, 1, 0, 1, 0, null, null, null,
        '2025-05-04 13:18:08.756488', '2025-05-04 13:18:08.756488', 'SYSTEM', 'SYSTEM', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('f35f6a87-ae16-46c7-9654-a4c8258f3351', 'SVC_MCSA_LIST', 'لیست خدمات متصل به یک حساب در کانال', '/mcsa/find', 1,
        1, 0, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 1, 0, null, null, null,
        '2025-05-03 16:23:08.078908', '2025-05-03 16:23:08.078908', 'SYSTEM', 'SYSTEM', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('f44c7c29-37db-41cd-bac7-7dbee1a2fd4e', 'SVC_SERVICE_CATEGORY_LIST', 'لیست دسته بندی سرویس های قدیمی',
        '/service-category/list', 1, 1, 0, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 3, 2, null, null, 1, 0,
        1, 1, null, null, null, '2025-05-03 10:38:17.688952', '2025-05-03 10:38:17.688952', 'SYSTEM', 'SYSTEM', null,
        null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('b10583fd-4d47-46bb-b6e0-5cb2be8d7103', 'SVC_SERVICE_CHANNEL_ACCESS_LIST',
        'لیست سرویس های ارائه شده بر روی کانال', '/channel-service-access/list', 1, 1, 0, null, null,
        '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 1, 0, null, null, null,
        '2025-04-30 16:16:59.011240', '2025-04-30 16:16:59.011240', 'SYSTEM', 'SYSTEM', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('1b5ac43d-3f44-4201-bd36-952ee2392bb7', 'SVC_ASSETS_PARENT', 'پرنت دارایی ها', '/assets', 1, 1, 1, null, null,
        null, 4, 5, null, null, 0, 0, 0, 0, null, null, null, '2024-07-13 10:14:02.373298',
        '2024-07-13 10:14:02.373298', 'Dariush Abdolahi', 'Dariush Abdolahi', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('c8a5232f-96d9-4e25-83ee-771c1b5d1af8', 'SVC_ASSETS_MEMBERSHIP_CHL_WDR_LIMIT',
        'بروزرسانی محدودیت برداشت از دارایی ترمینال', '/update-channel-access/max-withdrawal', 1, 1, 1, null, null,
        '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 0, 0, null, null, null,
        '2024-07-13 10:14:14.117951', '2024-07-13 10:14:14.117951', 'Dariush Abdolahi', 'Dariush Abdolahi', null, null,
        null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('bc2c8b74-038a-49d2-bf96-c18944f896b9', 'SVC_ASSETS_REVOKE_MEMBERSHIP_CHANNEL', 'سلب تخصیص دارایی از ترمینال',
        '/revoke-channel-access', 1, 1, 1, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0,
        0, 0, null, null, null, '2024-07-13 10:14:14.117951', '2024-07-13 10:14:14.117951', 'Dariush Abdolahi',
        'Dariush Abdolahi', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('ad8a741e-8c74-4f3e-a629-8f098bc3aa73', 'SVC_ASSETS_ASSIGN_MEMBERSHIP_CHANNEL', 'تخصیص دارایی به ترمینال',
        '/assign-channel-access', 1, 1, 1, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0,
        0, 0, null, null, null, '2024-07-13 10:14:14.117951', '2024-07-13 10:14:14.117951', 'Dariush Abdolahi',
        'Dariush Abdolahi', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('46acc735-44af-4065-aa13-c7c41882319c', 'SVC_ASSETS_LIST', 'لیست دارایی های یک کاربر', '/list', 1, 1, 1, null,
        null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 0, 0, null, null, null,
        '2024-07-14 10:54:06.320015', '2024-07-14 10:54:06.320015', 'Dariush Abdolahi', 'Dariush Abdolahi', null, null,
        null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('4e308927-e8e7-471a-a4c9-d05060924be3', 'SVC_ASSETS_FAVOURITE', 'تغییر علاقه مندی حساب',
        '/account/favorite-modification', 1, 1, 1, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null,
        1, 0, 0, 0, null, null, null, '2024-07-14 10:54:06.320015', '2024-07-14 10:54:06.320015', 'Dariush Abdolahi',
        'Dariush Abdolahi', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('9c13ebd9-ac83-4c8a-82bf-494577067fc8', 'SVC_ASSETS_FIND_ACCOUNT_MEMBERSHIP', 'یافتن دارایی اکانت با شناسه',
        '/local-account/{membershipId}', 1, 1, 1, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 3, 2, null, null,
        1, 0, 0, 0, null, null, null, '2024-07-14 11:44:44.648544', '2024-07-14 11:44:44.648544', 'Dariush Abdolahi',
        'Dariush Abdolahi', null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('e181c6c3-06b5-44eb-a197-c9c38892423d', 'SVC_ASSETS_LIST_LOCAL', 'لیست لوکال دارایی های یک کاربر',
        '/local-list', 1, 1, 1, null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 0, 0, null,
        null, null, '2024-07-14 10:54:06.320015', '2024-07-14 10:54:06.320015', 'Dariush Abdolahi', 'Dariush Abdolahi',
        null, null, null, null);
INSERT INTO REF.TBL_SCM_SERVICE (SERVICE_ID, CODE, TITLE, ALIAS, VERSION, STATUS, IS_SYSTEMIC, REQUEST_JSON_SCHEMA,
                                 RESPONSE_JSON_SCHEMA, PARENT_SERVICE_ID, SERVICE_TYPE_CODE, SERVICE_IMPL_TYPE_CODE,
                                 IMPL_SERVICE_PROVIDER_ID, IMPL_COMPOSITION_TYPE_CODE, CHECK_ACCESS_FIRST_AUTH,
                                 CHECK_ACCESS_SECOND_AUTH, CHECK_ACCESS_SERVICE, CHECK_ACCESS_ASSET,
                                 PROPERTY_NAME_AMOUNT, PROPERTY_NAME_ASSET, SERVICE_METADATA, CREATE_DATE,
                                 LAST_EDIT_DATE, CREATOR, LAST_EDITOR, PATH, HTTP_METHOD, REQUEST_CONTENT_TYPE,
                                 REQUEST_BODY_TYPE)
VALUES ('a1c7053d-9902-4f60-b597-0e6e39273da5', 'SVC_ASSETS_SYNC', 'همگام سازی دارایی های یک کاربر', '/sync', 1, 1, 1,
        null, null, '1b5ac43d-3f44-4201-bd36-952ee2392bb7', 1, 2, null, null, 1, 0, 0, 0, null, null, null,
        '2024-07-13 10:14:14.117951', '2024-07-13 10:14:14.117951', 'Dariush Abdolahi', 'Dariush Abdolahi', null, null,
        null, null);



INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'a1c7053d-9902-4f60-b597-0e6e39273da5', '2024-07-13 10:15:06.273168',
        '2024-07-13 10:15:06.273168', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'e181c6c3-06b5-44eb-a197-c9c38892423d', '2024-07-14 10:54:54.339563',
        '2024-07-14 10:54:54.339563', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '9c13ebd9-ac83-4c8a-82bf-494577067fc8', '2024-08-17 13:05:27.509557',
        '2024-08-17 13:05:27.509557', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '46acc735-44af-4065-aa13-c7c41882319c', '2024-08-27 09:48:48.215309',
        '2024-08-27 09:48:48.215309', 'Behdad Zabihi', 'Behdad Zabihi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc', '4e308927-e8e7-471a-a4c9-d05060924be3', '2024-04-23 09:47:20.238739',
        '2024-04-23 09:47:20.238739', 'Alireza Rayani', null);
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'ad8a741e-8c74-4f3e-a629-8f098bc3aa73', '2025-01-25 13:46:19.000000',
        '2024-04-06 09:52:44.370618', 'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'bc2c8b74-038a-49d2-bf96-c18944f896b9', '2024-04-06 09:52:44.370618',
        '2024-04-06 09:52:44.370618', 'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'c8a5232f-96d9-4e25-83ee-771c1b5d1af8', '2024-04-06 09:52:44.370618',
        '2024-04-06 09:52:44.370618', 'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', 'a1c7053d-9902-4f60-b597-0e6e39273da5', '2024-07-13 10:15:06.273168',
        '2024-07-13 10:15:06.273168', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', 'e181c6c3-06b5-44eb-a197-c9c38892423d', '2024-07-14 10:54:54.339563',
        '2024-07-14 10:54:54.339563', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', '9c13ebd9-ac83-4c8a-82bf-494577067fc8', '2024-08-17 13:05:27.509557',
        '2024-08-17 13:05:27.509557', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', '46acc735-44af-4065-aa13-c7c41882319c', '2024-08-27 09:48:48.215309',
        '2024-08-27 09:48:48.215309', 'Behdad Zabihi', 'Behdad Zabihi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', 'ad8a741e-8c74-4f3e-a629-8f098bc3aa73', '2025-01-25 13:46:19.000000',
        '2024-04-06 09:52:44.370618', 'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', 'bc2c8b74-038a-49d2-bf96-c18944f896b9', '2024-04-06 09:52:44.370618',
        '2024-04-06 09:52:44.370618', 'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('46f00b05-67d6-4b6e-8361-e7f295edcd04', 'c8a5232f-96d9-4e25-83ee-771c1b5d1af8', '2024-04-06 09:52:44.370618',
        '2024-04-06 09:52:44.370618', 'Dariush Abdolahi', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'e71af055-8b5b-4b5c-89b0-d811c8facdcb', '2024-06-22 13:01:43.844917',
        '2024-06-22 13:01:43.844917', 'Alireza Rayani', 'Dariush Abdolahi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'b10583fd-4d47-46bb-b6e0-5cb2be8d7103', '2024-04-06 09:52:44.185170',
        '2024-04-06 09:52:44.185170', 'Alireza Rayani', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'f44c7c29-37db-41cd-bac7-7dbee1a2fd4e', '2024-05-18 16:52:09.000000',
        '2024-05-18 16:52:11.000000', 'Alireza Rayani', 'Alireza Rayani');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'f35f6a87-ae16-46c7-9654-a4c8258f3351', '2024-04-06 09:52:44.294326',
        '2024-04-06 09:52:44.294326', 'Alireza Rayani', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'ed84c749-a9c2-4b61-a0ff-4bbdcd7b41d9', '2024-04-06 09:52:44.402128',
        '2024-04-06 09:52:44.402128', 'Alireza Rayani', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', 'f9fcb242-91bc-4278-b7cb-39a8f1447e7e', '2024-04-06 09:52:44.185170',
        '2024-04-06 09:52:44.185170', 'Alireza Rayani', 'Reza Jamshidi');
INSERT INTO REF.TBL_SCM_TERMINAL_SERVICE_ACCESS (TERMINAL_ID, SERVICE_ID, CREATE_DATE, LAST_EDIT_DATE, CREATOR,
                                                 LAST_EDITOR)
VALUES ('b9a79451-2141-40b6-98a0-72055a0042c5', '9f30c750-2aa1-4540-8ab9-87589cfdcd1e', '2024-04-06 09:52:44.402128',
        '2024-04-06 09:52:44.402128', 'Alireza Rayani', 'Reza Jamshidi');

---------------- CLIENT -----------------

INSERT INTO REF.USER_CHANNEL_AUTHENTICATION (ARCHIVE_NO, CHANNEL_ID, ACTIVE, AUTHENTICATION_METHOD_ID,
                                             USER_AUTHENTICATION_TYPE, USER_ID, FROM_DATE, TO_DATE, FIRST_PASSWORD,
                                             SECOND_PASSWORD, CHANNEL_ACCESS_PARAM, SECOND_LEVEL_AUTH_METHOD_ID,
                                             PRINT_COUNT, PASSWORD_SET_PRINTED, CREATED_BY, MODIFIED_BY, CREATION_DATE,
                                             MODIFICATION_DATE, EFFECTIVE_DATE, OTP_SERIAL_NO, STATE, NICK_NAME,
                                             BRANCH_CODE, PIN_BASED_PASSWORD, PATTERN_BASED_PASSWORD,
                                             LAST_DATE_OF_PASSWORD_CHANGE, LAST_REACTION_DATE_TO_PASSWORD, ABORT_PASS,
                                             USER_REASON, REASON, DE_ACTIVE_REASON)
VALUES (8, 2016, 1, 1, 3, 21000707, null, null, '52964e51911c9500d9a426ba3154f2d9', '52964e51911c9500d9a426ba3154f2d9',
        null, 1, 0, null, 101, 101, '2025-04-08 11:13:54.233722', '2025-04-08 11:13:54.233722', null, null, 1, 'refano',
        '', null, null, null, null, 0, null, null, null);


INSERT INTO REF.TBL_SUA_CLIENT (
    TERMINAL_CODE, CLIENT_AUTH_METHOD_BASIC, CLIENT_AUTH_METHOD_POST,
    CLIENT_AUTH_METHOD_SEC_JWT, CLIENT_AUTH_METHOD_KEY_JWT, REDIRECT_URIS,
    REQUIRE_AUTH_CONSENT, REQUIRE_PROOF_KEY, CHECK_VERSION, CHECK_ACTIVATION,
    SESSION_TTL_MINUTE, CREATE_DATE, LAST_EDIT_DATE, CREATOR, LAST_EDITOR,
    CLIENT_AUTH_METHOD_NONE, ALLOW_IP_ADDRESSES, CHECK_IP_ADDRESS,
    USER_CHANNEL_AUTHENTICATION_ID, STATUS, LEGAL_USER_ID
)
SELECT
    'FI', 1, 0, 0, 0,
    'http://10.10.8.46:3001/authorized,http://127.0.0.1:3001/authorized,http://10.10.8.122:3001/authorized', 1, 0,
    0, 0, 2073600, '2025-04-08 11:06:56.647044', '2025-04-08 11:06:56.647044',
    '6428007237', '6428007237', 1, '', 0,
    USER_CHANNEL_AUTHENTICATION_ID, 1, 22037200
FROM REF.USER_CHANNEL_AUTHENTICATION
WHERE CHANNEL_ID = 2016 AND NICK_NAME = 'refano';




INSERT INTO REF.USER_CHANNEL_AUTHENTICATION (ARCHIVE_NO, CHANNEL_ID, ACTIVE, AUTHENTICATION_METHOD_ID,
                                             USER_AUTHENTICATION_TYPE, USER_ID, FROM_DATE, TO_DATE, FIRST_PASSWORD,
                                             SECOND_PASSWORD, CHANNEL_ACCESS_PARAM, SECOND_LEVEL_AUTH_METHOD_ID,
                                             PRINT_COUNT, PASSWORD_SET_PRINTED, CREATED_BY, MODIFIED_BY, CREATION_DATE,
                                             MODIFICATION_DATE, EFFECTIVE_DATE, OTP_SERIAL_NO, STATE, NICK_NAME,
                                             BRANCH_CODE, PIN_BASED_PASSWORD, PATTERN_BASED_PASSWORD,
                                             LAST_DATE_OF_PASSWORD_CHANGE, LAST_REACTION_DATE_TO_PASSWORD, ABORT_PASS,
                                             USER_REASON, REASON, DE_ACTIVE_REASON)
VALUES (8, 2012, 1, 1, 3, 21000680, null, null, '', '', null, 1, 0, null, 101, 101, '2025-03-01 10:50:15.622965',
        '2025-03-01 10:50:15.622965', null, null, 1, 'NIB', '', null, null, null, null, 0, null, null, null);


UPDATE REF.TBL_SUA_CLIENT
SET TERMINAL_CODE                  = 'NIB',
    CLIENT_AUTH_METHOD_BASIC       = 1,
    CLIENT_AUTH_METHOD_POST        = 0,
    CLIENT_AUTH_METHOD_SEC_JWT     = 0,
    CLIENT_AUTH_METHOD_KEY_JWT     = 0,
    REDIRECT_URIS                  = 'http://127.0.0.1:3001/authorized,http://10.10.8.122:3001/authorized,http://10.10.8.46:3001/authorized',
    REQUIRE_AUTH_CONSENT           = 1,
    REQUIRE_PROOF_KEY              = 0,
    CHECK_VERSION                  = 0,
    CHECK_ACTIVATION               = 0,
    SESSION_TTL_MINUTE             = 1000,
    CREATE_DATE                    = '2025-03-01 10:42:36.228330',
    LAST_EDIT_DATE                 = '2025-03-01 11:36:43.240055',
    CREATOR                        = '6428007237',
    LAST_EDITOR                    = '6428007237',
    CLIENT_AUTH_METHOD_NONE        = 1,
    ALLOW_IP_ADDRESSES             = '0:0:0:0:0:0:0:1,10.10.8.133',
    CHECK_IP_ADDRESS               = 0,
    USER_CHANNEL_AUTHENTICATION_ID = (select USER_CHANNEL_AUTHENTICATION_ID
                                      from REF.USER_CHANNEL_AUTHENTICATION
                                      where CHANNEL_ID = 2012 and NICK_NAME = 'NIB'), STATUS = 1, LEGAL_USER_ID = 22037200
WHERE CLIENT_ID = 74;



INSERT INTO REF.USER_CHANNEL_AUTHENTICATION (ARCHIVE_NO, CHANNEL_ID, ACTIVE, AUTHENTICATION_METHOD_ID,
                                             USER_AUTHENTICATION_TYPE, USER_ID, FROM_DATE, TO_DATE, FIRST_PASSWORD,
                                             SECOND_PASSWORD, CHANNEL_ACCESS_PARAM, SECOND_LEVEL_AUTH_METHOD_ID,
                                             PRINT_COUNT, PASSWORD_SET_PRINTED, CREATED_BY, MODIFIED_BY, CREATION_DATE,
                                             MODIFICATION_DATE, EFFECTIVE_DATE, OTP_SERIAL_NO, STATE, NICK_NAME,
                                             BRANCH_CODE, PIN_BASED_PASSWORD, PATTERN_BASED_PASSWORD,
                                             LAST_DATE_OF_PASSWORD_CHANGE, LAST_REACTION_DATE_TO_PASSWORD, ABORT_PASS,
                                             USER_REASON, REASON, DE_ACTIVE_REASON)
VALUES (8, 22, 1, 1, 3, 21000705, null, null, 'a9ca352d4f0d288bf6a5228ff075fc4c', 'a9ca352d4f0d288bf6a5228ff075fc4c',
        null, 1, 0, null, 101, 101, '2025-03-12 07:52:05.297527', '2025-03-12 07:52:05.297527', null, null, 1, 'SCM',
        '', null, null, null, null, 0, null, null, null);


UPDATE REF.TBL_SUA_CLIENT
SET TERMINAL_CODE                  = 'NIB',
    CLIENT_AUTH_METHOD_BASIC       = 1,
    CLIENT_AUTH_METHOD_POST        = 0,
    CLIENT_AUTH_METHOD_SEC_JWT     = 0,
    CLIENT_AUTH_METHOD_KEY_JWT     = 0,
    REDIRECT_URIS                  = 'http://127.0.0.1:3001/authorized,http://10.10.8.122:3001/authorized,http://10.10.8.46:3001/authorized',
    REQUIRE_AUTH_CONSENT           = 1,
    REQUIRE_PROOF_KEY              = 0,
    CHECK_VERSION                  = 0,
    CHECK_ACTIVATION               = 0,
    SESSION_TTL_MINUTE             = 1000,
    CREATE_DATE                    = '2025-03-01 10:42:36.228330',
    LAST_EDIT_DATE                 = '2025-03-01 11:36:43.240055',
    CREATOR                        = '6428007237',
    LAST_EDITOR                    = '6428007237',
    CLIENT_AUTH_METHOD_NONE        = 1,
    ALLOW_IP_ADDRESSES             = '0:0:0:0:0:0:0:1,10.10.8.133',
    CHECK_IP_ADDRESS               = 0,
    USER_CHANNEL_AUTHENTICATION_ID = (
        SELECT USER_CHANNEL_AUTHENTICATION_ID
        FROM REF.USER_CHANNEL_AUTHENTICATION
        WHERE CHANNEL_ID = 2012 AND NICK_NAME = 'NIB'
    ),
    STATUS                         = 1,
    LEGAL_USER_ID                  = 22037200
WHERE CLIENT_ID = 74;






