package ir.daneshrefah.scm.uaa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * Creates the optional legacy MB/PWA activation datasource.
 */
@Configuration
@ConditionalOnProperty(
        prefix = "scm.uaa.datasource.activation",
        name = "enabled",
        havingValue = "true"
)
@Conditional(ActivationDataSourceConfig.ActivationDataSourceUrlCondition.class)
@EnableConfigurationProperties(DataSourceConfigProperties.class)
public class ActivationDataSourceConfig {

    @Bean("activationDataSource")
    public DataSource activationDataSource(DataSourceConfigProperties properties) {
        DataSourceConfigProperties.DatasourceProperties activation = properties.getActivation();
        HikariDataSource dataSource = DataSourceBuilder.create(getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(activation.getDriverClassName())
                .url(activation.getUrl())
                .username(activation.getUsername())
                .password(activation.getPassword())
                .build();
        dataSource.setMaximumPoolSize(activation.getMaxConnection());
        return dataSource;
    }

    /** Treat an absent or blank legacy URL as an intentionally unconfigured optional datasource. */
    public static class ActivationDataSourceUrlCondition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return StringUtils.hasText(context.getEnvironment().getProperty("scm.uaa.datasource.activation.url"));
        }
    }
}
