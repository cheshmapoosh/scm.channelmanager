package ir.daneshrefah.scm.config;

import ir.daneshrefah.scm.config.model.property.EnvironmentProperty;
import ir.daneshrefah.scm.config.service.PropertyService;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentProperties;
import org.springframework.cloud.config.server.environment.JdbcEnvironmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

//@Component
public class DatabaseEnvironmentRepository extends JdbcEnvironmentRepository  {

    private final PropertyService propertyService;

    public DatabaseEnvironmentRepository(JdbcTemplate jdbc, JdbcEnvironmentProperties properties, PropertyService propertyService) {
        super(jdbc, properties);
        this.propertyService = propertyService;
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        if (null == label || label.isEmpty()) {
        label = "test";
        }
        if (!profile.startsWith("default")) {
            profile = "default," + profile;
        }    String[] profiles = StringUtils.commaDelimitedListToStringArray(profile);
        Environment environment = new Environment(application, profiles, label, null, null);
        String config = application;    if (!config.startsWith("default")) {
            config = "default," + config;    }
        String[] configs = StringUtils.commaDelimitedListToStringArray(config);
        List<String> envs = new ArrayList<>(new LinkedHashSet<>(Arrays.asList(profiles)));    List<String> applications = new ArrayList<>(new LinkedHashSet<>(Arrays.asList(configs)));
        Collections.reverse(applications);
        Collections.reverse(envs);
        for (String app : applications) {
            for (String env : envs) {
            addPropertySource(environment, app, env, label);
            }
        }    return environment;
    }
    private void addPropertySource(Environment environment, String application, String profile, String label) {

        List<EnvironmentProperty> properties = propertyService.findProperties(application, profile, label);
        for (EnvironmentProperty property : properties) {
            String name = property.getApplicationId() + "-" + property.getProfileId();
            if (null != property.getLabelKey()) {
                name = name + "-" + property.getLabelKey();
            }
            name = name + "-" + property.getPropKey();
            PropertySource source = new PropertySource( name,
                    Collections.singletonMap(property.getPropKey(), property.getPropValue()));
            environment.getPropertySources().add(source);
        }
    }

//    @Override
//    public Environment findOne(String application, String profile, String label, boolean includeOrigin) {
//        return EnvironmentRepository.super.findOne(application, profile, label, includeOrigin);
//    }

}
