package ir.daneshrefah.scm.config;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-02
 */
//@Component
//@Repository
public class CacheableJdbcEnvironmentRepository extends JdbcEnvironmentRepository {

    public CacheableJdbcEnvironmentRepository(JdbcTemplate jdbc, JdbcEnvironmentProperties properties, PropertiesResultSetExtractor extractor) {
        super(jdbc, properties, extractor);
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        return super.findOne(application, profile, label);
    }

    /*@Override
    @Cacheable(value = "configProperties", key = "{#application, #profile, #label}")
    public Environment findOne(String application, String profile, String label) {
        return super.findOne(application, profile, label);
    }*/

    //    @Override
//    @Cacheable(value = "configProperties", key = "{#application, #profile, #label}")
//    public Environment findOne(String application, String profile, String label) {
//        return super.findOne(application, profile, label);
//    }

}
