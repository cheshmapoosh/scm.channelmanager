package ir.daneshrefah.scm.core.integration.component;

import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponentProvider;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public class ServiceComponentPoolGenerator extends RouteBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceComponentPoolGenerator.class);

    private ServiceComponentProvider serviceComponentProvider;
    private List<ServiceComponent> serviceComponents;

    public ServiceComponentPoolGenerator(ServiceComponentProvider serviceComponentProvider,
                                         List<ServiceComponent> serviceComponents) {
        this.serviceComponentProvider = serviceComponentProvider;
        this.serviceComponents = serviceComponents;
    }

    @Override
    public void configure() {
        for (Iterator<ServiceComponent> iterator = serviceComponents.iterator(); iterator.hasNext(); ) {
            ServiceComponent serviceComponent = iterator.next();
            String fromUri = "direct:SVC_" + serviceComponentProvider.getCode() + "_" + serviceComponent.getCode();
            String toUri = serviceComponentProvider.getCode() + ":" + serviceComponent.getCode();
            from(fromUri)
                    .log("serviceComponent Call")
                    .to(toUri)
                    .end();
            LOGGER.info("serviceComponent with source '{}' and target '{}' registered.", fromUri, toUri);
        }

    }

}
