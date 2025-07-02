package ir.daneshrefah.scm.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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

@Configuration("coreJpaConfig")
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = {"ir.daneshrefah.scm.core",
                "ir.daneshrefah.scm.task",
                "ir.daneshrefah.scm.config",
                "ir.daneshrefah.scm.notification.client",
                "ir.daneshrefah.scm.common.data",
                "ir.daneshrefah.scm.cache",
                "ir.daneshrefah.scm.repository"
        }, excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        value = ir.daneshrefah.scm.common.data.repository.logging.LogTraceRepository.class
),
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EntityScan(basePackages = {"ir.daneshrefah.scm.core",
        "ir.daneshrefah.scm.task",
        "ir.daneshrefah.scm.config",
        "ir.daneshrefah.scm.notification.client",
        "ir.daneshrefah.scm.common.data",
        "ir.daneshrefah.scm.cache"

})
@Primary
public class JpaConfig {

    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("primaryDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder) {

        Map<String, Object> props = new HashMap<>();
        props.put("hibernate.hbm2ddl.auto", "none");
        props.put("hibernate.dialect", "org.hibernate.dialect.DB2Dialect");
        props.put("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");

        return builder
                .dataSource(dataSource)
                .packages(
                        "ir.daneshrefah.scm.core.entity",
                        "ir.daneshrefah.scm.task.entity",
                        "ir.daneshrefah.scm.config.entity",
                        "ir.daneshrefah.scm.notification.client.entity",
                        "ir.daneshrefah.scm.common.data.entity",
//                        "ir.daneshrefah.scm.entity",
                        "ir.daneshrefah.scm.cache.entity"
                )
                .properties(props)
                .build();
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean emf) {
        return new JpaTransactionManager(Objects.requireNonNull(emf.getObject()));
    }

}
