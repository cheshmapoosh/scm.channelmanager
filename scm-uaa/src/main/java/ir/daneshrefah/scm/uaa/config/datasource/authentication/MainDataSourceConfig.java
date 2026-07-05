package ir.daneshrefah.scm.uaa.config.datasource.authentication;

import com.zaxxer.hikari.HikariDataSource;
import ir.daneshrefah.scm.uaa.config.DataSourceConfigProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Owns the required UAA authentication persistence unit.
 *
 * <p>Only repositories below {@code repository.authentication} are created by this configuration.
 * Shared entity packages are included solely so UAA entity relationships can be mapped; their
 * repository interfaces are deliberately not scanned here.</p>
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties(DataSourceConfigProperties.class)
@EnableJpaRepositories(
        basePackages = {
                "ir.daneshrefah.scm.uaa.repository.authentication",
                "ir.daneshrefah.scm.common.data.repository",
                "ir.daneshrefah.scm.notification.client.repository",
        },
        entityManagerFactoryRef = "mainEntityManagerFactory",
        transactionManagerRef = "mainTransactionManager"
)
public class MainDataSourceConfig {

    @Bean("mainDataSource")
    @Primary
    public DataSource mainDataSource(DataSourceConfigProperties properties) {
        DataSourceConfigProperties.DatasourceProperties main = properties.getMain();
        if (!main.isEnabled()) {
            throw new IllegalStateException("scm.uaa.datasource.main is required and cannot be disabled");
        }
        HikariDataSource dataSource = DataSourceBuilder.create(getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(main.getDriverClassName())
                .url(main.getUrl())
                .username(main.getUsername())
                .password(main.getPassword())
                .build();
        dataSource.setMaximumPoolSize(main.getMaxConnection());
        return dataSource;
    }

    @Bean("mainJdbcTemplate")
    @Primary
    public JdbcTemplate mainJdbcTemplate(@Qualifier("mainDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("mainNamedParameterJdbcTemplate")
    @Primary
    public NamedParameterJdbcTemplate mainNamedParameterJdbcTemplate(
            @Qualifier("mainDataSource") DataSource dataSource
    ) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean("mainEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean mainEntityManagerFactory(
            DataSourceConfigProperties dataSourceConfigProperties,
            @Qualifier("mainDataSource") DataSource dataSource,
            EntityManagerFactoryBuilder builder
    ) {
        Map<String, Object> jpaProperties = new HashMap<>();
        jpaProperties.put("hibernate.default_schema", dataSourceConfigProperties.getMain().getDefaultSchema());
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "false");
        jpaProperties.put(
                "hibernate.physical_naming_strategy",
                "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
        );

        return builder
                .dataSource(dataSource)
                .packages(
                        "ir.daneshrefah.scm.uaa.repository.authentication",
                        "ir.daneshrefah.scm.common.data.entity",
                        "ir.daneshrefah.scm.notification.client.repository.entity",
                        "ir.daneshrefah.scm.common.data.converter"
                )
                .properties(jpaProperties)
                .build();
    }

    @Bean("mainTransactionManager")
    @Primary
    public PlatformTransactionManager mainTransactionManager(
            @Qualifier("mainEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory
    ) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}
