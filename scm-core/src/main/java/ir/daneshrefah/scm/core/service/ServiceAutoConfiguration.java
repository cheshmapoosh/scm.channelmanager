package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.service.DirectServiceImplementation;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.service.ServiceService;
import jakarta.annotation.PostConstruct;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
@Component
public class ServiceAutoConfiguration extends RouteBuilder {

    @Autowired
    private ServiceService serviceService;

    @PostConstruct
    public void init() {
    }

    @Override
    public void configure() throws Exception {
        List<Service> serviceList = serviceService.findServiceList();
        for (Iterator<Service> iterator = serviceList.iterator(); iterator.hasNext(); ) {
            Service service = iterator.next();
            from("direct:SERVICE_" + service.getCode())
                    .log("receive service call: " + service.getCode())
//                    .setBody().constant("Helloooooo2")
//                    .bean(nabRequestTransformer)
//                    .to("log:request?showAll=true")
//                    .log("body is : ${body}")
//                    .setHeader(Exchange.CONTENT_TYPE, simple("application/json"))
                    .to("direct:" + ((DirectServiceImplementation) service.getImplementation()).getServiceRelations().get(0).getServiceComponent().getCode())
//                    .to("log:response?showAll=true")
//                    .bean(nabResponseTransformer)
                    .end();
        }

        System.out.println("now");
    }


}
