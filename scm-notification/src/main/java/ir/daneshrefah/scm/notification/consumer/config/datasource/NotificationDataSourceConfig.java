package ir.daneshrefah.scm.notification.consumer.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.notification.consumer.config.NotificationConfiguration;
import ir.daneshrefah.scm.notification.consumer.config.prop.ConfigProperties;
import ir.daneshrefah.scm.utils.log.LogUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@ConditionalOnProperty(name = "scm.notification.data-source.url")
@ConditionalOnBean(NotificationConfiguration.class)
@Slf4j
@Configuration
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.notification.consumer",
        entityManagerFactoryRef = "notificationEntityManagerFactory",
        transactionManagerRef = "notificationTransactionManager"
)
@RequiredArgsConstructor
public class NotificationDataSourceConfig {

    @Bean
    public CommandLineRunner initNotificationDataSourceConfig() {
        return args -> {
            log.info(LogUtils.markWith(LogUtils.Color.PURPLE, ">>> [notification-consumer] datasource configuration successfully loaded."));
        };
    }

    @Bean
    public DataSource notificationDataSource(ConfigProperties properties) {
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
            ConfigProperties configProperties,
            @Qualifier("notificationDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("ir.daneshrefah.scm.notification.consumer")
                .properties(createConfigProperties(configProperties))
                .build();
    }

    private Map<String, ?> createConfigProperties(ConfigProperties configProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.DEFAULT_SCHEMA, configProperties.getDataSource().getDefaultSchema());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "false");
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return properties;
    }

    @Bean
    public PlatformTransactionManager notificationTransactionManager(
            @Qualifier("notificationEntityManagerFactory") LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }


}
