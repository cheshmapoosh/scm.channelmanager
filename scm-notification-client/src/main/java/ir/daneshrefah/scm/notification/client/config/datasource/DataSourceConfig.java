package ir.daneshrefah.scm.notification.client.config.datasource;


import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.notification.client.config.ConfigProperties;
import ir.daneshrefah.scm.notification.client.config.NotificationConfigProperties;
import ir.daneshrefah.scm.notification.client.service.spec.NotificationService;
import ir.daneshrefah.scm.utils.log.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @apiNote scm:
 * notification:
 * enabled: true
 * datasource:
 * url: jdbc:db2://10.10.4.104:50001/DBREFSW
 * username: db2inst1
 * password: db2inst1
 * driver-class-name: com.ibm.db2.jcc.DB2Driver
 * max-connection: 10
 * default-schema: REF
 * @since 2024-02-07
 */
@Slf4j
@Configuration
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = ConfigProperties.BASE_PACKAGE,
        entityManagerFactoryRef = ConfigProperties.ENTITY_MANAGER_FACTORY_REF,
        transactionManagerRef = ConfigProperties.TX_MANGER_REF_NAME
)
@RequiredArgsConstructor
@ConditionalOnProperty(ConfigProperties.SCM_NOTIFICATION_DATASOURCE_URL)
public class DataSourceConfig {

    @Bean
    public CommandLineRunner init(NotificationConfigProperties properties) {
        return args -> {
            if (properties.isEnabled()) {
                log.info(LogUtils.markWith(LogUtils.Color.GREEN, ">>> notification configuration successfully initialized."));
            } else {
                log.warn(LogUtils.markWith(LogUtils.Color.YELLOW,">>> notification functionality has been disabled."));
            }
        };
    }

    @Bean
    public DataSource notificationDataSource(NotificationConfigProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(properties.getDataSource().getDriverClassName())
                .url(properties.getDataSource().getUrl())
                .username(properties.getDataSource().getUsername())
                .password(properties.getDataSource().getPassword())
                .build();
        dataSource.setMaximumPoolSize(properties.getDataSource().getMaxConnection());
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean notificationEntityManagerFactory(
            NotificationConfigProperties dataSourceConfigProperties,
            @Qualifier(ConfigProperties.NOTIFICATION_DATA_SOURCE_BEAN) DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages(ConfigProperties.BASE_PACKAGE)
                .properties(createConfigProperties(dataSourceConfigProperties))
                .build();
    }

    private Map<String, ?> createConfigProperties(NotificationConfigProperties dataSourceConfigProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.DEFAULT_SCHEMA, dataSourceConfigProperties.getDataSource().getDefaultSchema());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "false");
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, ConfigProperties.HIBERNATE_PHYSICAL_NAMING_STRATEGY);
        return properties;
    }

    @Bean
    public PlatformTransactionManager notificationTransactionManager(
            @Qualifier(ConfigProperties.ENTITY_MANAGER_FACTORY_REF) LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }

    @Bean
    @Scope("singleton")
    public NotificationService notificationService
            (NotificationConfigProperties configProperties
                    , @Qualifier("notificationServiceImpl") NotificationService activeNotificationService
                    , @Qualifier("disabledNotificationServiceImpl") NotificationService disabledNotificationService) {
        return configProperties.isEnabled() ? activeNotificationService : disabledNotificationService;
    }

}
