package ir.daneshrefah.scm.process.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-23
 */
@Configuration
//@ConditionalOnProperty(name = "scm.process.enabled", havingValue = "true")
public class ProcessConfiguration {

    @Value("${scm.process.jdbc-url}")
    private String jdbcUrl;
    @Value("${scm.process.jdbc-driver-class-name}")
    private String jdbcDriverClassName;
    @Value("${scm.process.jdbc-username}")
    private String jdbcUsername;
    @Value("${scm.process.jdbc-password}")
    private String jdbcPassword;

    @Bean
//    @ConfigurationProperties(prefix = "camunda.bpm.configuration")
    public ProcessEngineConfiguration processEngineConfiguration() {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
        processEngineConfiguration.setJdbcUrl(jdbcUrl);
        processEngineConfiguration.setJdbcDriver(jdbcDriverClassName);
        processEngineConfiguration.setJdbcUsername(jdbcUsername);
        processEngineConfiguration.setJdbcPassword(jdbcPassword);
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
