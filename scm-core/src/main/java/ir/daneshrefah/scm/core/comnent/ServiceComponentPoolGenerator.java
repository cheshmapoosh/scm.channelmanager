package ir.daneshrefah.scm.core.comnent;

import ir.daneshrefah.scm.common.model.component.ServiceComponent;
import ir.daneshrefah.scm.common.model.component.ServiceComponentProvider;
import ir.daneshrefah.scm.service.ServiceComponentService;
import org.apache.camel.builder.RouteBuilder;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public class ServiceComponentPoolGenerator extends RouteBuilder {

    private ServiceComponentService serviceComponentService;
    private ServiceComponentProvider serviceComponentProvider;



    @Override
    public void configure() throws Exception {
        List<ServiceComponent> serviceComponents = serviceComponentService.findListByServiceComponentProvider((String) serviceComponentProvider.getId());
        for (Iterator<ServiceComponent> iterator = serviceComponents.iterator(); iterator.hasNext(); ) {
            ServiceComponent serviceComponent = iterator.next();
            from("direct:"  + serviceComponent.getCode())
                    .log("receive nab provider")
//                    .setBody().constant("Helloooooo2")
//                    .bean(nabRequestTransformer)
//                    .to("log:request?showAll=true")
//                    .log("body is : ${body}")
//                    .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
                    .to(serviceComponentProvider.getCode() + ":" + serviceComponent.getCode())
//                    .to("log:response?showAll=true")
//                    .bean(nabResponseTransformer)
                    .end();
        }
    }

    public void setServiceComponentService(ServiceComponentService serviceComponentService) {
        this.serviceComponentService = serviceComponentService;
    }

    public void setServiceComponentProvider(ServiceComponentProvider serviceComponentProvider) {
        this.serviceComponentProvider = serviceComponentProvider;
    }
}
