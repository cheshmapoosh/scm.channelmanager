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
//        return super.findOne(application, profile, label);
        if (!StringUtils.contains(profile, "default")) {
            profile = "default, " + profile;
        }

        List<PropertySource> propertySources = super.findOne(application, profile, label).getPropertySources();
        Environment environment = new Environment(application,profile);
        List<PropertySource> mergedPropertySources = mergePropertySources(propertySources);
        for (PropertySource property : mergedPropertySources) {
            environment.getPropertySources().add(property);
        }
        return environment;
    }

    public  List<PropertySource> mergePropertySources(List<PropertySource> propertySources) {
        List<PropertySource> mergedList = new ArrayList<>();

        for (PropertySource source : propertySources) {
            mergeOrAddSource(mergedList, source);
        }

        return mergedList;
    }

    private void mergeOrAddSource(List<PropertySource> mergedList, PropertySource source) {
        for (PropertySource mergedSource : mergedList) {
            if (areSourcesEqual(mergedSource.getSource(), source.getSource())) {
                mergedSource.getName();
                return;
            }
        }
        mergedList.add(new PropertySource(mergedList.isEmpty() ? source.getName() : mergedList.get(0).getName(), source.getSource()));
    }

    private boolean areSourcesEqual(Map<?, ?> source1, Map<?, ?> source2) {
        return source1.equals(source2);
    }
}
