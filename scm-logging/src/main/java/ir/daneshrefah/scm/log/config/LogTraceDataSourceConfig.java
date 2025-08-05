package ir.daneshrefah.scm.log.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"ir.daneshrefah.scm.common.log.repository.logging"},
        entityManagerFactoryRef = "logTraceEntityManagerFactory",
        transactionManagerRef = "logTransactionManager")
public class LogTraceDataSourceConfig {

    @Bean("logDataSource")
    @Primary
    public DataSource logDataSource(LogApplication properties) {
        DatasourceProperties datasourceProperties = properties.getDatasource().getLogTrace();
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader()).type(HikariDataSource.class).driverClassName(datasourceProperties.getDriverClassName()).url(datasourceProperties.getUrl()).username(datasourceProperties.getUsername()).password(datasourceProperties.getPassword()).build();
        dataSource.setSchema(datasourceProperties.getDefaultSchema());
        dataSource.setMaximumPoolSize(datasourceProperties.getMaxConnection());
        return dataSource;
    }

    @Bean(name = "logTraceEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean logTraceEntityManagerFactory(@Qualifier("logDataSource") DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan("ir.daneshrefah.scm.common.log.entity.logging");
        em.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.DB2Dialect");
        em.setJpaPropertyMap(properties);
        return em;
    }

    @Bean(name = "logTransactionManager")
    @Primary
    public PlatformTransactionManager logTransactionManager(@Qualifier("logTraceEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
