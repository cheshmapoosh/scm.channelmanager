package ir.daneshrefah.scm.notification.client.config.datasource;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.notification.client.config.NotificationConfig;
import ir.daneshrefah.scm.notification.client.config.prop.ClientConfigProperties;
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
@ConditionalOnBean(NotificationConfig.class)
@Slf4j
@Configuration
@EnableConfigurationProperties
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.notification.client",
        entityManagerFactoryRef = "clientNotificationEntityManagerFactory",
        transactionManagerRef = "clientNotificationTransactionManager"
)
@RequiredArgsConstructor
public class ClientNotificationDataSourceConfig {

    @Bean
    public CommandLineRunner initClientNotificationDataSourceConfig() {
        return args -> {
            log.info(LogUtils.markWith(LogUtils.Color.PURPLE, ">>> [notification-producer] datasource configuration successfully loaded."));
        };
    }

    @Bean
    public DataSource clientNotificationDataSource(ClientConfigProperties properties) {
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
    public LocalContainerEntityManagerFactoryBean clientNotificationEntityManagerFactory(
            ClientConfigProperties configProperties,
            @Qualifier("clientNotificationDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(dataSource)
                .packages("ir.daneshrefah.scm.notification.client")
                .properties(createConfigProperties(configProperties))
                .build();
    }

    private Map<String, ?> createConfigProperties(ClientConfigProperties configProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(AvailableSettings.DEFAULT_SCHEMA, configProperties.getDataSource().getDefaultSchema());
        properties.put(AvailableSettings.SHOW_SQL, "true");
        properties.put(AvailableSettings.FORMAT_SQL, "false");
        properties.put(AvailableSettings.PHYSICAL_NAMING_STRATEGY, "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return properties;
    }

    @Bean
    public PlatformTransactionManager clientNotificationTransactionManager(
            @Qualifier("clientNotificationEntityManagerFactory") LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }


}
