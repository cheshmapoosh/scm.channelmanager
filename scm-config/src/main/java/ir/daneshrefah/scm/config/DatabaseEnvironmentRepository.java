package ir.daneshrefah.scm.config;

import ir.daneshrefah.scm.config.model.property.EnvironmentProperty;
import ir.daneshrefah.scm.config.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class DatabaseEnvironmentRepository implements EnvironmentRepository {

    private final PropertyService propertyService;

    @Autowired
    public DatabaseEnvironmentRepository(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        List<EnvironmentProperty> properties = propertyService.findProperties(application, profile, label);
        Environment environment = new Environment(application, profile);
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

        return environment;
    }

//    @Override
//    public Environment findOne(String application, String profile, String label, boolean includeOrigin) {
//        return EnvironmentRepository.super.findOne(application, profile, label, includeOrigin);
//    }

}
