package ir.daneshrefah.scm.config;

import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-11-18
 */
//@Component
public class DatabaseEnvironmentRepository implements EnvironmentRepository {

//    private final PropertiesRepository propertiesRepository;
    private final PropertyService propertyService;

    public DatabaseEnvironmentRepository(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        List<Property> properties = propertyService.find(application, profile, label);
        Environment environment = new Environment(application, profile);
        for (Property property : properties) {
            String name = property.getApplicationKey() + "-" + property.getProfileKey();
            if (null != property.getLabelKey()) {
                name = name + "-" + property.getLabelKey();
            }
            name = name + "-" + property.getPropKey();
            PropertySource source = new PropertySource( name,
                    Collections.singletonMap(property.getPropKey(), property.getPropValue()));
            environment.getPropertySources().add(source);
        }

        return environment;
    }

//    @Override
//    public Environment findOne(String application, String profile, String label, boolean includeOrigin) {
//        return EnvironmentRepository.super.findOne(application, profile, label, includeOrigin);
//    }

}
