package ir.daneshrefah.scm.core.integration.component;

import ir.daneshrefah.scm.core.service.ErrorMappingService;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.core.service.ServiceComponentProviderService;
import ir.daneshrefah.scm.core.service.ServiceComponentService;
import ir.daneshrefah.scm.plugin.api.component.AbstractComponent;
import ir.daneshrefah.scm.utils.io.ClassLoader;
import jakarta.annotation.PostConstruct;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Configuration
public class ServiceComponentAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceComponentAutoConfiguration.class);

    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ConfigurableBeanFactory beanFactory;
    @Autowired
    private ServiceComponentProviderService serviceComponentProviderService;
    @Autowired
    private ServiceComponentService serviceComponentService;
    @Autowired
    private ErrorMappingService errorMappingService;

    @PostConstruct
    public void init() {
        List<ServiceComponentProvider> serviceComponentProviders = serviceComponentProviderService.findAllServiceComponentProviders();
        LOGGER.info("start define serviceComponentProviders, '{}' provider found.", serviceComponentProviders.size());
        for (Iterator<ServiceComponentProvider> iterator = serviceComponentProviders.iterator(); iterator.hasNext(); ) {
            ServiceComponentProvider serviceComponentProvider = iterator.next();
            String componentName = serviceComponentProvider.getCode();
            String componentMetadata = serviceComponentProvider.getMetadata();
            String componentClassName = serviceComponentProvider.getComponentClassName();
            AbstractComponent component = ClassLoader.createInstanceOfClass(componentClassName,
                    AbstractComponent.class, componentMetadata);
            if (null == component) {
                LOGGER.warn("error on create instance of '{}' component with className '{}'", componentName, componentClassName);
                continue;
            }
            camelContext.addComponent(componentName, component);
            LOGGER.info("component '{}' successfully added to context with '{}' class and '{}' metadata", componentName,
                    componentClassName, componentMetadata);
            List<ServiceComponent> serviceComponents = serviceComponentService.findListByServiceComponentProvider(
                    serviceComponentProvider.getId());
            if (null == serviceComponents || serviceComponents.isEmpty()) {
                LOGGER.warn("no serviceComponent found for serviceComponentProvider '{}'", serviceComponentProvider.getCode());
                continue;
            }
            ServiceComponentPoolGenerator serviceComponentPoolGenerator = new ServiceComponentPoolGenerator(
                    serviceComponentProvider, serviceComponents, errorMappingService);
            beanFactory.registerSingleton("serviceComponentRouteBuilder_" + serviceComponentProvider.getCode(),
            serviceComponentPoolGenerator);
        }

    }
}
