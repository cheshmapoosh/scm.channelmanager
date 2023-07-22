package ir.daneshrefah.scm.core.comnent;

import ir.daneshrefah.scm.common.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.connector.nab.NabComponent;
import ir.daneshrefah.scm.core.inbound.RestInboundChannelGenerator;
import ir.daneshrefah.scm.service.ServiceComponentProviderService;
import ir.daneshrefah.scm.service.ServiceComponentService;
import jakarta.annotation.PostConstruct;
import org.apache.camel.CamelContext;
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

    @Autowired
    private CamelContext camelContext;
    @Autowired
    private ConfigurableBeanFactory beanFactory;
    @Autowired
    private ServiceComponentProviderService serviceComponentProviderService;
    @Autowired
    private ServiceComponentService serviceComponentService;

    @PostConstruct
    public void init() {
        List<ServiceComponentProvider> serviceComponentProviders = serviceComponentProviderService.findAllServiceComponentProviders();
        for (Iterator<ServiceComponentProvider> iterator = serviceComponentProviders.iterator(); iterator.hasNext(); ) {
            ServiceComponentProvider serviceComponentProvider = iterator.next();

            camelContext.addComponent(serviceComponentProvider.getCode(), new NabComponent()); //TODO load class dynamically

            ServiceComponentPoolGenerator serviceComponentPoolGenerator = new ServiceComponentPoolGenerator();
            serviceComponentPoolGenerator.setServiceComponentProvider(serviceComponentProvider);
            serviceComponentPoolGenerator.setServiceComponentService(serviceComponentService);
            beanFactory.registerSingleton("serviceComponentRouteBuilder" + serviceComponentProvider.getCode(),
                    serviceComponentPoolGenerator);
        }

    }
}
