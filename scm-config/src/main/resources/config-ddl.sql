
CREATE TABLE "REF"."TBL_SFG_PROFILE"  (
    "PROFILE_ID" DECIMAL(22,0) NOT NULL GENERATED ALWAYS AS IDENTITY (
        START WITH +1
        INCREMENT BY +1
        MINVALUE +1
        MAXVALUE +9999999999999999999999
        NO CYCLE
        CACHE 20
        NO ORDER ),
    "CODE" VARCHAR(255 OCTETS) NOT NULL,
    "TITLE" VARCHAR(255 OCTETS),
    "CREATE_DATE" TIMESTAMP NOT NULL,
    "CREATOR_USER" VARCHAR(255) NOT NULL,
    "LAST_EDIT_DTE" TIMESTAMP NOT NULL,
    "LAST_EDITOR_USER" VARCHAR(255) NOT NULL,
    PRIMARY KEY (PROFILE_ID)
);

INSERT INTO "REF".TBL_SFG_PROFILE (CODE, TITLE, CREATE_DATE, CREATOR_USER, LAST_EDIT_DTE, LAST_EDITOR_USER) VALUES
                                ('dev','توسعه', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
                                ('test-dd','تست بانکداری مجازی', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
                                ('test-ns','تست خدمات الکترونیک', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
                                ('pre-production','پایلوت', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
                                ('production','عملیاتی', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, '');

CREATE TABLE "REF"."TBL_SFG_APPLICATION"  (
    "APPLICATION_ID" DECIMAL(22,0) NOT NULL GENERATED ALWAYS AS IDENTITY (
        START WITH +1
        INCREMENT BY +1
        MINVALUE +1
        MAXVALUE +9999999999999999999999
        NO CYCLE
        CACHE 20
        NO ORDER ),
    "CODE" VARCHAR(255 OCTETS) NOT NULL,
    "TITLE" VARCHAR(255 OCTETS),
    "CREATE_DATE" TIMESTAMP NOT NULL,
    "CREATOR_USER" VARCHAR(255) NOT NULL,
    "LAST_EDIT_DTE" TIMESTAMP NOT NULL,
    "LAST_EDITOR_USER" VARCHAR(255) NOT NULL,
    PRIMARY KEY (APPLICATION_ID)
);

INSERT INTO "REF".TBL_SFG_APPLICATION (CODE,TITLE, CREATE_DATE, CREATOR_USER, LAST_EDIT_DTE, LAST_EDITOR_USER) VALUES
               ('application','تنظیمات کلی', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
               ('scm-app','سامانه مدیریت کانال', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
               ('scm-cache','سامانه حافظه پنهان', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, ''),
               ('scm-uaa','سامانه مدیریت کاربران', CURRENT TIMESTAMP, '', CURRENT TIMESTAMP, '');
-- DROP TABLE "REF".TBL_SFG_PROPERTY;

CREATE TABLE "REF".TBL_SFG_PROPERTY (
    "PROPERTY_ID" DECIMAL(22,0) NOT NULL GENERATED ALWAYS AS IDENTITY (
        START WITH +1
        INCREMENT BY +1
        MINVALUE +1
        MAXVALUE +9999999999999999999999
        NO CYCLE
        CACHE 20
        NO ORDER ),
    APPLICATION_ID DECIMAL(22,0) NOT NULL,
    PROFILE_ID DECIMAL(22,0) NOT NULL,
    LABEL_KEY VARCHAR(255),
    TITLE VARCHAR(550) NOT NULL,
    PROP_KEY VARCHAR(255) NOT NULL,
    PROP_VALUE VARCHAR(2500) NOT NULL,
    "CREATE_DATE" TIMESTAMP NOT NULL,
    "CREATOR_USER" VARCHAR(255) NOT NULL,
    "LAST_EDIT_DTE" TIMESTAMP NOT NULL,
    "LAST_EDITOR_USER" VARCHAR(255) NOT NULL,
    FOREIGN KEY (APPLICATION_ID) REFERENCES REF.TBL_SFG_APPLICATION (APPLICATION_ID),
    FOREIGN KEY (PROFILE_ID) REFERENCES REF.TBL_SFG_PROFILE (PROFILE_ID),
    PRIMARY KEY (PROPERTY_ID)
);

INSERT INTO "REF".TBL_SFG_PROPERTY (APPLICATION_ID,PROFILE_ID,LABEL_KEY,PROP_KEY,PROP_VALUE,CREATE_DATE,CREATOR_USER,LAST_EDIT_DTE,LAST_EDITOR_USER,TITLE) VALUES
        (1,2,'master','test1','test1','2023-12-02 10:59:37.127065','Elahe','2023-12-02 10:59:37.127065','Elahe',NULL),
        (3,1,NULL,'hazelcast.config.instance-name','my-instance3','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.clusterName','dev3','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'spring.datasource.url','jdbc:db2://10.10.4.104:50001/DBREFSW','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'spring.datasource.username','db2inst1','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'spring.datasource.password','db2inst1','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'spring.datasource.driver-class-name','com.ibm.db2.jcc.DB2Driver','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'spring.jpa.properties.hibernate.default_schema','REF','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'spring.jpa.show-sql','true','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.properties.hazelcast.socket.bind.any','true','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL);
INSERT INTO "REF".TBL_SFG_PROPERTY (APPLICATION_ID,PROFILE_ID,LABEL_KEY,PROP_KEY,PROP_VALUE,CREATE_DATE,CREATOR_USER,LAST_EDIT_DTE,LAST_EDITOR_USER,TITLE) VALUES
        (3,1,NULL,'hazelcast.config.properties.hazelcast.logging.type','slf4j','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.properties.hazelcast.partition.table.send.interval','15','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.properties.hazelcast.partition.count','271','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.port','5701','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.port-auto-increment','true','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.port-count','100','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.reuse-address','true','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.join.multicast-config.enabled','false','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.join.aws-config.enabled','false','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.join.tcp-ip-config.enabled','true','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL);
INSERT INTO "REF".TBL_SFG_PROPERTY (APPLICATION_ID,PROFILE_ID,LABEL_KEY,PROP_KEY,PROP_VALUE,CREATE_DATE,CREATOR_USER,LAST_EDIT_DTE,LAST_EDITOR_USER,TITLE) VALUES
        (3,1,NULL,'hazelcast.config.network-config.join.tcp-ip-config.required-member','127.0.0.1','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.join.tcp-ip-config.members','127.0.0.1','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.network-config.join.tcp-ip-config.connection-timeout-seconds','5','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.flake-id-generator-configs.default.prefetch-count','10000','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.flake-id-generator-configs.default.prefetch-validity-millis','600000','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.flake-id-generator-configs.default.node-id-offset','0','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (3,1,NULL,'hazelcast.config.flake-id-generator-configs.default.statistics-enabled','true','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL),
        (1,1,NULL,'test1','test2','2023-12-02 10:59:37.127065','Elahe','2023-12-02 10:59:37.127065','Elahe',NULL),
        (1,2,'master','test3','test3','2023-12-02 10:59:37.127065','Elahe','2023-12-02 10:59:37.127065','Elahe',NULL),
        (5,1,NULL,'scm.test.name','test test','2023-12-02 10:59:37.127065','Elahe','2023-12-02 10:59:37.127065','Elahe',NULL);
INSERT INTO "REF".TBL_SFG_PROPERTY (APPLICATION_ID,PROFILE_ID,LABEL_KEY,PROP_KEY,PROP_VALUE,CREATE_DATE,CREATOR_USER,LAST_EDIT_DTE,LAST_EDITOR_USER,TITLE) VALUES
        (3,2,NULL,'hazelcast.config.clusterName','dev333','2023-12-02 15:22:51.0','Dariush','2023-12-02 15:23:00.0','Dariush',NULL);