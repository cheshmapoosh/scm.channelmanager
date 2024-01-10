package ir.daneshrefah.scm.uaa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Configuration
public class DataSourceConfig {

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
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("authenticationDataSource") DataSource dataSource,
            DataSourceConfigProperties dataSourceProperties) {

        Map<String, Object> properties = new HashMap<>();
        if (null != dataSourceProperties.getAuthenticationDatasource().getDefaultSchema())
            properties.put("hibernate.default_schema", dataSourceProperties.getActivationDatasource().getDefaultSchema());

        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder.dataSource(dataSource)
                .packages("ir.daneshrefah.scm.uaa.repository.authentication",
                        "ir.daneshrefah.scm.common.data.entity")
                .properties(properties)
                .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean activationEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("activationDataSource") DataSource dataSource,
            DataSourceConfigProperties dataSourceProperties) {

        Map<String, Object> properties = new HashMap<>();
        if (null != dataSourceProperties.getActivationDatasource().getDefaultSchema())
            properties.put("hibernate.default_schema", dataSourceProperties.getActivationDatasource().getDefaultSchema());

        return builder.dataSource(dataSource)
                .packages("ir.daneshrefah.scm.uaa.repository.activation")
                .properties(properties)
                .build();
    }

}
