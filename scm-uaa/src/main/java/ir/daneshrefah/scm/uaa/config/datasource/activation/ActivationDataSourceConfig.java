package ir.daneshrefah.scm.uaa.config.datasource.activation;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.uaa.config.DataSourceConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Owns the optional legacy MB/PWA activation persistence unit.
 * Remove this configuration after legacy activation compatibility is retired.
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DataSourceConfigProperties.class)
@ConditionalOnProperty(
        prefix = "scm.uaa.datasource.activation",
        name = "enabled",
        havingValue = "true"
)
@Conditional(ActivationDataSourceConfig.ActivationDataSourceUrlCondition.class)
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.uaa.repository.activation",
        entityManagerFactoryRef = "activationEntityManagerFactory",
        transactionManagerRef = "activationTransactionManager"
)
public class ActivationDataSourceConfig {

    @Bean("activationDataSource")
    public DataSource activationDataSource(DataSourceConfigProperties properties) {
        DataSourceConfigProperties.DatasourceProperties activation = properties.getActivation();
        HikariDataSource dataSource = DataSourceBuilder.create(getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(activation.getDriverClassName())
                .url(activation.getUrl())
                .username(activation.getUsername())
                .password(activation.getPassword())
                .build();
        dataSource.setMaximumPoolSize(activation.getMaxConnection());
        return dataSource;
    }

    @Bean("activationEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean activationEntityManagerFactory(
            DataSourceConfigProperties dataSourceConfigProperties,
            @Qualifier("activationDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.default_schema", dataSourceConfigProperties.getActivation().getDefaultSchema());
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "false");
        jpaProperties.put(
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        );

        return builder
                .dataSource(dataSource)
                .packages("ir.daneshrefah.scm.uaa.repository.activation")
                .properties(jpaProperties)
                .build();
    }

    @Bean("activationTransactionManager")
    public PlatformTransactionManager activationTransactionManager(
            @Qualifier("activationEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory
    ) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }

    /** Treat an absent or blank legacy URL as an intentionally unconfigured optional datasource. */
    public static class ActivationDataSourceUrlCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return StringUtils.hasText(context.getEnvironment().getProperty("scm.uaa.datasource.activation.url"));
        }
    }
}
