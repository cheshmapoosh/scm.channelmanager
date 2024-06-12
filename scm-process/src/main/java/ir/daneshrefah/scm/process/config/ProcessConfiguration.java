package ir.daneshrefah.scm.process.config;

import com.hazelcast.shaded.com.zaxxer.hikari.HikariDataSource;

import org.camunda.bpm.engine.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
//@EnableConfigurationProperties(ProcessProperties.class) //TODO uncomment
@Configuration
//@ConditionalOnProperty(name = "scm.process.enabled", havingValue = "true")//TODO uncomment
public class ProcessConfiguration {

    @Value("${scm.process.jdbc-url}")
    private String jdbcUrl;

    @Value("${scm.process.jdbc-driver-class-name}")
    private String jdbcDriverClassName;

    @Value("${scm.process.jdbc-username}")
    private String jdbcUsername;

    @Value("${scm.process.jdbc-password}")
    private String jdbcPassword;

    @Value("${scm.process.jdbc-schema}")
    private String jdbcSchema;


    @Bean
//    @ConfigurationProperties(prefix = "camunda.bpm.configuration")
    public ProcessEngineConfiguration processEngineConfiguration() {
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
     /*   processEngineConfiguration.setJdbcUrl(jdbcUrl);
        processEngineConfiguration.setJdbcDriver(jdbcDriverClassName);
        processEngineConfiguration.setJdbcUsername(jdbcUsername);
        processEngineConfiguration.setJdbcPassword(jdbcPassword);
        processEngineConfiguration.setJdbc*/
//        processEngineConfiguration.setDatabaseSchemaUpdate(jdbcSchema);
        processEngineConfiguration.setHistory(ProcessEngineConfiguration.HISTORY_FULL);
        processEngineConfiguration.setJobExecutorActivate(true);
        processEngineConfiguration.setDataSource(dataSource());
//        processEngineConfiguration.hissetEnableCmdExceptionLogging(true);
//        procesetDatabaseSchemassEngineConfiguration.setHistoryLevelCommand(ProcessEngineConfiguration.HISTORY_AUDIT); // Adjust history level as needed
//        processEngineConfiguration.setHistoryLevelCommand(new HistoryLevelSetupCommand()); // Adjust history level as needed
        return processEngineConfiguration;
    }
    @Bean //TODO remove this method
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setUsername(jdbcUsername);
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setPassword(jdbcPassword);
        dataSource.setDriverClassName(jdbcDriverClassName);
        dataSource.setMaximumPoolSize(10);
        dataSource.setSchema(jdbcSchema);
        return dataSource;
    }

//    @Bean//TODO uncomment
//    public DataSource processDataSource(ProcessProperties properties) {
//        com.zaxxer.hikari.HikariDataSource dataSource = DataSourceBuilder.create(this.getClass().getClassLoader())
//                .type(com.zaxxer.hikari.HikariDataSource.class)
//                .driverClassName(properties.getDatasource().getDriverClassName())
//                .url(properties.getDatasource().getUrl())
//                .username(properties.getDatasource().getUsername())
//                .password(properties.getDatasource().getPassword())
//                .build();
//        dataSource.setSchema(properties.getDatasource().getDefaultSchema());
//        Integer maximumPoolSize = properties.getDatasource().getMaxConnection();
//        if (null != maximumPoolSize) {
//            dataSource.setMaximumPoolSize(maximumPoolSize);
//        }
//        return dataSource;
//    }
//@Bean //TODO uncomment
//public ProcessEngineConfiguration processEngineConfiguration(@Qualifier("processDataSource") DataSource dataSource) {
//    ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
//    processEngineConfiguration.setDataSource(dataSource);
//    processEngineConfiguration.setHistory(ProcessEngineConfiguration.HISTORY_AUDIT);
////        processEngineConfiguration.hissetEnableCmdExceptionLogging(true);
//
////        processEngineConfiguration.setHistoryLevelCommand(ProcessEngineConfiguration.HISTORY_AUDIT); // Adjust history level as needed
////        processEngineConfiguration.setHistoryLevelCommand(new HistoryLevelSetupCommand()); // Adjust history level as needed
//    return processEngineConfiguration;
//}
    @Bean
    public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration) {
        return processEngineConfiguration.buildProcessEngine();
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean
    public IdentityService identityService(ProcessEngine processEngine) {
        return processEngine.getIdentityService();
    }
}