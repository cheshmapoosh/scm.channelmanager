package ir.daneshrefah.scm.logging.config;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.common.log.repository.logging.LogTraceRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AutoConfiguration
@EnableTransactionManagement
@EntityScan(basePackages = "ir.daneshrefah.scm.common.data.entity.logging")
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.common.log.repository.logging",
        entityManagerFactoryRef = "logEntityManagerFactory",
        transactionManagerRef = "logTransactionManager",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = LogTraceRepository.class
        )
)
//@ConditionalOnProperty(value = "scm.logging.datasource.enabled", havingValue = "true", matchIfMissing = false)
public class LogDataSourceConfig {


    @Bean
//    @ConditionalOnProperty(value = "scm.logging.datasource.enabled", havingValue = "true", matchIfMissing = false)
    public DataSource logDataSource(ApplicationContext ctx, DataSourceConfigProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(properties.getDatasource().getDriverClassName())
                .url(properties.getDatasource().getUrl())
                .username(properties.getDatasource().getUsername())
                .password(properties.getDatasource().getPassword())
                .build();
        dataSource.setSchema(properties.getDatasource().getDefaultSchema());
        dataSource.setMaximumPoolSize(properties.getDatasource().getMaxConnection());
        return dataSource;
    }

    @Bean(name = "logEntityManagerFactory")
//    @ConditionalOnProperty(value = "scm.logging.datasource.enabled", havingValue = "true", matchIfMissing = false)
    public LocalContainerEntityManagerFactoryBean logEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("logDataSource") DataSource logDataSource) {
        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.DB2Dialect");
        props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
        return builder
                .dataSource(logDataSource)
                .packages("ir.daneshrefah.scm.common.log.entity.logging")
                .properties(props)
                .build();
    }

    @Bean(name = "logTransactionManager")
//    @ConditionalOnProperty(value = "scm.logging.datasource.enabled", havingValue = "true", matchIfMissing = false)
    public PlatformTransactionManager logTransactionManager(
            @Qualifier("logEntityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(Objects.requireNonNull(emf.getObject()));
    }
}
