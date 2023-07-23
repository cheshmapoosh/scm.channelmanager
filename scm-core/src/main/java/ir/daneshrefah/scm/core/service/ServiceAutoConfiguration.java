package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.*;
import ir.daneshrefah.scm.service.ServiceService;
import jakarta.annotation.PostConstruct;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
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
            RouteDefinition routeDefinition = from("direct:SERVICE_" + service.getCode())
                    .process(exchange -> {
                        Message message = exchange.getMessage().getBody(Message.class);
                        System.out.printf("service %s is called by correlationId %s \n",
                                message.getHeader().getService().getTerminalServiceAccess().getService().getCode(),
                                message.getHeader().getCorrelationId());
                    });

            if (ServiceType.DIRECT.equals(service.getType())) {
                DirectServiceImplementation implementation = (DirectServiceImplementation) service.getImplementation();
                for (Iterator<ServiceRelation> serviceIterator = implementation.getServiceRelations().iterator(); serviceIterator.hasNext(); ) {
                    ServiceRelation serviceRelation = serviceIterator.next();
                    AbstractTransformer requestTransformer = null;
                    AbstractTransformer responseTransformer = null;
                    if (StringUtils.isNotEmpty(serviceRelation.getRequestTransformerClass())) {
                        Class<? extends AbstractTransformer> requestTransformerClass = (Class<? extends AbstractTransformer>) Class.forName(serviceRelation.getRequestTransformerClass());
                        Constructor<?> constructor = requestTransformerClass.getConstructor();
                        requestTransformer = (AbstractTransformer) constructor.newInstance();
                    }
                    if (StringUtils.isNotEmpty(serviceRelation.getResponseTransformerClass())) {
                        Class<? extends AbstractTransformer> responseTransformerClass = (Class<? extends AbstractTransformer>) Class.forName(serviceRelation.getResponseTransformerClass());
                        Constructor<?> constructor = responseTransformerClass.getConstructor();
                        responseTransformer = (AbstractTransformer) constructor.newInstance();
                    }
                    if (null != requestTransformer) {
                        AbstractTransformer finalRequestTransformer = requestTransformer;
                        routeDefinition = routeDefinition.process(exchange -> {
                            Message message = exchange.getMessage().getBody(Message.class);
                            Object request = finalRequestTransformer.transform("", "", message);
                            message.setInput(request);
//                            exchange.getMessage().setBody(message);
                        });
                    }
//                    if (null != responseTransformer)
//                        message = responseTransformer.transform("", "", message);
                    routeDefinition = routeDefinition.to("direct:" + serviceRelation.getServiceComponent().getCode());
                }
                if (implementation.getServiceRelations().isEmpty())
                    routeDefinition.setBody().constant("No implementation found for service: " + service.getCode());
            }
            routeDefinition.end();
        }

    }


}
