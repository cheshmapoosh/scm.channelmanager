package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.core.service.ServiceServiceImpl;
import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
@Component
public class ServiceAutoConfiguration extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAutoConfiguration.class);

    @Autowired
    private ServiceServiceImpl serviceService;
    @Autowired
    private ExternalServiceExecutor externalServiceExecutor;
    @Autowired
    private JavaServiceExecutor javaServiceExecutor;
    @Autowired
    private CompositionServiceExecutor compositionServiceExecutor;
    private final Map<ServiceImplementationType, ServiceExecutor> executorMap = new HashMap<>();

    public ServiceAutoConfiguration() {
    }

    @Override
    public void configure() {
        executorMap.put(ServiceImplementationType.EXTERNAL, externalServiceExecutor);
        executorMap.put(ServiceImplementationType.JAVA, javaServiceExecutor);
        executorMap.put(ServiceImplementationType.COMPOSITION, compositionServiceExecutor);
        List<Service> services = serviceService.findCallableServiceList();
        LOGGER.info("service list load completed. count: {}", services.size());
        for (Iterator<Service> iterator = services.iterator(); iterator.hasNext(); ) {
            Service service = iterator.next();
            if (ServiceImplementationType.PARENT.equals(service.getImplementationType())) {
                continue;
            }
            if (ServiceStatus.INACTIVE.equals(service.getStatus())) {
                continue;
            }
            String fromUri = "SVI_" + service.getCode();

            if (ServiceImplementationType.EXTERNAL.equals(service.getImplementationType())) {
                externalServiceExecutor.registerExternalServiceProvider(((ExternalService) service).getServiceProvider());
            }

            LOGGER.info("start define service '{}' with uri '{}'", service.getId(), fromUri);
            RouteDefinition routeDefinition = from("direct:" + fromUri).routeId("ROUTE_" + fromUri);
            routeDefinition.log("service call: " + service.getCode());
//            routeDefinition = service.getImplementation().fullFill(routeDefinition);
            routeDefinition.process(exchange -> {
                ServiceExecutor serviceExecutor = executorMap.get(service.getImplementationType());
                serviceExecutor.executeService(service, exchange.getMessage().getBody(Message.class));
            });
            routeDefinition.end();
        }
    }

}
