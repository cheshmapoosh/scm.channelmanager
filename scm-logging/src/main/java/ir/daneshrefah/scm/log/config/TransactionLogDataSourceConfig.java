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
        basePackages = "ir.daneshrefah.scm.common.log.repository.transaction",
        entityManagerFactoryRef = "transactionLogEntityManagerFactory",
        transactionManagerRef = "transactionLogTransactionManager"
)
@Conditional(LogConditions.TransactionLogTraceCondition.class)
@Slf4j
public class TransactionLogDataSourceConfig {

    @PostConstruct
    public void init() {
        log.info(">>> TransactionLogDataSourceConfig successfully initialized");
    }

    @Bean(name = "transactionLogDataSource")
    public DataSource transactionLogDataSource(LogApplication logApplication) {
        DatasourceProperties datasourceProperties = logApplication.getDatasource().getTransactionLog();
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

    @Bean(name = "transactionLogEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean transactionLogEntityManagerFactory(
            @Qualifier("transactionLogDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ir.daneshrefah.scm.common.log.entity.transaction");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
//        properties.put("hibernate.dialect", "org.hibernate.dialect.DB2Dialect");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "transactionLogTransactionManager")
    public PlatformTransactionManager transactionLogTransactionManager(
            @Qualifier("transactionLogEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
