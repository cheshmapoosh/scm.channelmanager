package ir.daneshrefah.scm.config;

import org.springframework.cloud.config.server.environment.JdbcEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepository;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-02
 */
@Configuration
public class EnvironmentRepositoryConfiguration {

    @Bean
    public JdbcEnvironmentRepository jdbcEnvironmentRepository(JdbcTemplate jdbcTemplate,
                                                               JdbcEnvironmentRepository.PropertiesResultSetExtractor extractor,
                                                               JdbcEnvironmentProperties environmentProperties) {
        return new CacheableJdbcEnvironmentRepository(jdbcTemplate, environmentProperties, extractor);
    }

//    @Bean
//    public JdbcEnvironmentRepository jdbcEnvironmentRepository(JdbcEnvironmentRepositoryFactory factory,
//                                                               JdbcEnvironmentProperties environmentProperties) {
//        return factory.build(environmentProperties);
//    }

}
