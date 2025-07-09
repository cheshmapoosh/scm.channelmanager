package ir.daneshrefah.scm.uaa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.uaa.repository.activation",
        entityManagerFactoryRef = "activationEntityManagerFactory",
        transactionManagerRef = "activationTransactionManager"
        , excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = ir.daneshrefah.scm.common.data.repository.logging.LogTraceRepository.class
)
)
@EnableConfigurationProperties(DataSourceConfigProperties.class)
public class ActivationDataSourceConfig {

    @Bean
    public DataSource activationDataSource(DataSourceConfigProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(properties.getActivationDatasource().getDriverClassName())
                .url(properties.getActivationDatasource().getUrl())
                .username(properties.getActivationDatasource().getUsername())
                .password(properties.getActivationDatasource().getPassword())
                .build();
        dataSource.setMaximumPoolSize(properties.getActivationDatasource().getMaxConnection());
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean activationEntityManagerFactory(
            DataSourceConfigProperties dataSourceConfigProperties,
            @Qualifier("activationDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.default_schema", dataSourceConfigProperties.getActivationDatasource().getDefaultSchema());

        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return builder
                .dataSource(dataSource)
                .packages("ir.daneshrefah.scm.uaa.repository.activation")
                .properties(properties)
                .build();
    }

    @Bean
    public PlatformTransactionManager activationTransactionManager(
            @Qualifier("activationEntityManagerFactory") LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }

}