package ir.daneshrefah.scm.config.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CustomJdbcEnvironmentRepository extends JdbcEnvironmentRepository {


    public CustomJdbcEnvironmentRepository(JdbcTemplate jdbc, JdbcEnvironmentProperties properties, PropertiesResultSetExtractor extractor) {
        super(jdbc, properties, extractor);
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        if (!StringUtils.contains(profile, "default")) {
            profile = "default," + profile;
        }
        return super.findOne(application, profile, label);
    }

}
