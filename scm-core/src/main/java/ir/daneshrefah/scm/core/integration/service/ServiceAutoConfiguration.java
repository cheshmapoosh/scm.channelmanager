package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.plugin.api.model.service.Service;
import ir.daneshrefah.scm.core.service.ServiceService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

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
    private ServiceService serviceService;

    @Override
    public void configure() throws Exception {
        List<Service> services = serviceService.findServiceList();
        LOGGER.info("service list load completed. count: {}", services.size());
        for (Iterator<Service> iterator = services.iterator(); iterator.hasNext(); ) {
            Service service = iterator.next();
            String fromUri = "direct:SVI_" + service.getCode();
            RouteDefinition routeDefinition = from(fromUri);
            routeDefinition.log("service call");
            routeDefinition = service.getImplementation().fullFill(routeDefinition);
            routeDefinition.end();
        }
    }

}
