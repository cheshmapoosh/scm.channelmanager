package ir.daneshrefah.scm.logging.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "ir.daneshrefah.scm.logging", entityManagerFactoryRef = "logEntityManagerFactory", transactionManagerRef = "logTransactionManager")
@ConditionalOnProperty(value = "scm.logging.datasource.enabled", havingValue = "true")
public class LogDataSourceConfig {

    @Bean
    public DataSource logDataSource(DataSourceConfigProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader()).type(HikariDataSource.class)
                .driverClassName(properties.getDatasource().getDriverClassName())
                .url(properties.getDatasource().getUrl())
                .username(properties.getDatasource().getUsername())
                .password(properties.getDatasource().getPassword()).build();
        dataSource.setMaximumPoolSize(properties.getDatasource().getMaxConnection());
        return dataSource;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean logEntityManagerFactory(DataSourceConfigProperties dataSourceConfigProperties,
                                                                          @Qualifier("logDataSource") DataSource dataSource,
                                                                          EntityManagerFactoryBuilder builder) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.default_schema", dataSourceConfigProperties.getDatasource().getDefaultSchema());
        properties.put("hibernate.show_sql", "true");
        properties.put("hibernate.format_sql", "false");
        properties.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return builder.dataSource(dataSource).packages("ir.daneshrefah.scm.logging").properties(properties).build();
    }

    @Bean
    public PlatformTransactionManager logTransactionManager(@Qualifier("logEntityManagerFactory") LocalContainerEntityManagerFactoryBean todosEntityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(todosEntityManagerFactory.getObject()));
    }

}