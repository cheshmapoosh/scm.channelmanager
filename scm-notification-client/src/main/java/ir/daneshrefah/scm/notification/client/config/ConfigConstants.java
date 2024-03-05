package ir.daneshrefah.scm.notification.client.config;

public interface ConfigConstants {

    //Spring JPA Data properties
    String BASE_PACKAGE = "ir.daneshrefah.scm.notification";
    String ENTITY_MANAGER_FACTORY_REF = "notificationEntityManagerFactory";
    String TX_MANGER_REF_NAME = "notificationTransactionManager";
    String NOTIFICATION_DATA_SOURCE_BEAN = "notificationDataSource";

    //Hibernate config properties
    String HIBERNATE_PHYSICAL_NAMING_STRATEGY = "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy";

}

