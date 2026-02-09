package ir.daneshrefah.scm.log.config;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.common.log.configuration.LogConditions;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.common.log.repository.message",
        entityManagerFactoryRef = "messageLogEntityManagerFactory",
        transactionManagerRef = "messageLogTransactionManager"
)
@Conditional(LogConditions.MessageLogCondition.class)
@Slf4j
public class MessageLogDataSourceConfig {

    @PostConstruct
    public void init() {
        log.info(">>> MessageLogDataSourceConfig successfully initialized");
    }

    @Bean(name = "messageLogDataSource")
    public DataSource messageLogDataSource(LogApplication logApplication) {
        DatasourceProperties datasourceProperties = logApplication.getDatasource().getMessageLog();
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .url(datasourceProperties.getUrl())
                .driverClassName(datasourceProperties.getDriverClassName())
                .username(datasourceProperties.getUsername())
                .password(datasourceProperties.getPassword())
                .build();
        dataSource.setSchema(datasourceProperties.getDefaultSchema());
        dataSource.setMaximumPoolSize(datasourceProperties.getMaxConnection());
        return dataSource;
    }

    @Bean(name = "messageLogEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean transactionLogEntityManagerFactory(
            @Qualifier("messageLogDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ir.daneshrefah.scm.common.log.entity.message");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.DB2Dialect");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "messageLogTransactionManager")
    public PlatformTransactionManager transactionLogTransactionManager(
            @Qualifier("messageLogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
