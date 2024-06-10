package ir.daneshrefah.scm.process.config;

import com.zaxxer.hikari.HikariDataSource;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-23
 */
@EnableConfigurationProperties(ProcessProperties.class)
@Configuration
@ConditionalOnProperty(name = "scm.process.enabled", havingValue = "true")
public class ProcessConfiguration {

    @Bean
    public DataSource processDataSource(ProcessProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
                .type(HikariDataSource.class)
                .driverClassName(properties.getDatasource().getDriverClassName())
                .url(properties.getDatasource().getUrl())
                .username(properties.getDatasource().getUsername())
                .password(properties.getDatasource().getPassword())
                .build();
        dataSource.setSchema(properties.getDatasource().getDefaultSchema());
        Integer maximumPoolSize = properties.getDatasource().getMaxConnection();
        if (null != maximumPoolSize) {
            dataSource.setMaximumPoolSize(maximumPoolSize);
        }
        return dataSource;
    }

    @Bean
    public ProcessEngineConfiguration processEngineConfiguration(@Qualifier("processDataSource") DataSource dataSource) {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        processEngineConfiguration.setDataSource(dataSource);
        processEngineConfiguration.setHistory(ProcessEngineConfiguration.HISTORY_AUDIT);
//        processEngineConfiguration.hissetEnableCmdExceptionLogging(true);

//        processEngineConfiguration.setHistoryLevelCommand(ProcessEngineConfiguration.HISTORY_AUDIT); // Adjust history level as needed
//        processEngineConfiguration.setHistoryLevelCommand(new HistoryLevelSetupCommand()); // Adjust history level as needed
        return processEngineConfiguration;
    }

    @Bean
    public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration) {
        return processEngineConfiguration.buildProcessEngine();
    }

    /*@Bean
    public ProcessEngine processEngine() {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration
                .createStandaloneInMemProcessEngineConfiguration()
//                .setHistoryLevelCommand(HistoryLevel.HISTORY_LEVEL_AUDIT)
                .setDatabaseSchemaUpdate("true") // Update schema on startup (optional)
                .setJdbcUrl(jdbcUrl) // Configure your database connection
                .setJdbcDriver(jdbcDriverClassName) // Configure your database connection
                .setJdbcUsername(jdbcUsername)
                .setJdbcPassword(jdbcPassword);

        return processEngineConfiguration.buildProcessEngine();
    }*/

}
