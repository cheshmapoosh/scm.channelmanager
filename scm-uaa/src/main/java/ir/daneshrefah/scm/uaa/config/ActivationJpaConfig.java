package ir.daneshrefah.scm.uaa.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
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

@Configuration
@ConditionalOnBean(name = "activationDataSource")
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "ir.daneshrefah.scm.uaa.repository.activation",
        entityManagerFactoryRef = "activationEntityManagerFactory",
        transactionManagerRef = "activationTransactionManager",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = ir.daneshrefah.scm.common.log.repository.logging.LogTraceRepository.class
        )
)
public class ActivationJpaConfig {

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
}
