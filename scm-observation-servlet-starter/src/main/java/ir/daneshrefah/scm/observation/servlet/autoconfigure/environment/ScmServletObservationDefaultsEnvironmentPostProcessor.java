package ir.daneshrefah.scm.observation.servlet.autoconfigure.environment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

public final class ScmServletObservationDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    static final String PROPERTY_SOURCE_NAME = "scmServletObservationDefaults";
    private static final String DEFAULTS_RESOURCE = "META-INF/scm/servlet-observation-defaults.yml";

    private final YamlPropertySourceLoader propertySourceLoader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment == null) {
            return;
        }

        ClassLoader classLoader = application == null
                ? ScmServletObservationDefaultsEnvironmentPostProcessor.class.getClassLoader()
                : application.getClassLoader();
        Resource resource = new ClassPathResource(DEFAULTS_RESOURCE, classLoader);
        if (!resource.exists() || !resource.isReadable()) {
            return;
        }

        List<PropertySource<?>> defaults;
        try {
            defaults = propertySourceLoader.load(PROPERTY_SOURCE_NAME, resource);
        } catch (IOException ignored) {
            return;
        }

        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : defaults) {
            if (!propertySources.contains(propertySource.getName())) {
                propertySources.addLast(propertySource);
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
