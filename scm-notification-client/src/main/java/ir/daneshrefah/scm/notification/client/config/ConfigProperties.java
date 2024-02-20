package ir.daneshrefah.scm.notification.client.config;

public interface ConfigProperties {

    //Spring JPA Data properties
    String BASE_PACKAGE = "ir.daneshrefah.scm.notification.client";
    String ENTITY_MANAGER_FACTORY_REF = "notificationEntityManagerFactory";
    String TX_MANGER_REF_NAME = "notificationTransactionManager";
    String NOTIFICATION_DATA_SOURCE_BEAN = "notificationDataSource";

    //Hibernate config properties
    String HIBERNATE_PHYSICAL_NAMING_STRATEGY = "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy";

    //Notification config properties
    String SCM_NOTIFICATION_BASE_PREFIX = "scm.notification";
    String SCM_NOTIFICATION_STATUS = SCM_NOTIFICATION_BASE_PREFIX+".enabled";
    String SCM_NOTIFICATION_DATASOURCE = SCM_NOTIFICATION_BASE_PREFIX+".data-source";
    String SCM_NOTIFICATION_DATASOURCE_URL = SCM_NOTIFICATION_DATASOURCE+".url";
    String SCM_NOTIFICATION_SMS_CONFIG = SCM_NOTIFICATION_BASE_PREFIX+".sms-config";
}

