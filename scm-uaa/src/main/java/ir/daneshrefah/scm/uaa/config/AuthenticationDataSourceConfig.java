package ir.daneshrefah.scm.uaa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
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
 * @since 2024-01-09
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {
        "ir.daneshrefah.scm.common.data",
        "ir.daneshrefah.scm.notification",
        "ir.daneshrefah.scm.common.log.repository.transaction",
        "ir.daneshrefah.scm.uaa.repository.authentication"},
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
        , excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = ir.daneshrefah.scm.common.log.repository.logging.LogTraceRepository.class
)
)


public class AuthenticationDataSourceConfig {

    @Bean
    @Primary
    public DataSource authenticationDataSource(DataSourceConfigProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(properties.getAuthenticationDatasource().getDriverClassName())
                .url(properties.getAuthenticationDatasource().getUrl())
                .username(properties.getAuthenticationDatasource().getUsername())
                .password(properties.getAuthenticationDatasource().getPassword())
                .build();
        dataSource.setMaximumPoolSize(properties.getAuthenticationDatasource().getMaxConnection());
        return dataSource;
    }

    @Primary
    @Bean("entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean authenticationEntityManagerFactory(
            DataSourceConfigProperties dataSourceConfigProperties,
            @Qualifier("authenticationDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.default_schema", dataSourceConfigProperties.getAuthenticationDatasource().getDefaultSchema());

        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder
                .dataSource(dataSource)
                .packages("ir.daneshrefah.scm.uaa.repository.authentication",
                        "ir.daneshrefah.scm.notification",
                        "ir.daneshrefah.scm.common.log.entity.transaction",
                        "ir.daneshrefah.scm.common.data.repository",
                        "ir.daneshrefah.scm.common.data")
                .properties(properties)
                .build();
    }

    @Bean("transactionManager")
    @Primary
    public PlatformTransactionManager authenticationTransactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }

}
