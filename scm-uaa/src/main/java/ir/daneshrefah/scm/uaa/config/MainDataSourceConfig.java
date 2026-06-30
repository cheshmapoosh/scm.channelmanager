package ir.daneshrefah.scm.uaa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Creates the required UAA datasource. UAA cannot start when this datasource is disabled.
 */
@Configuration
@EnableConfigurationProperties(DataSourceConfigProperties.class)
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
}
