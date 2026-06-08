package ir.daneshrefah.scm.core.integration.processor;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("firstStrategy")
public class FirstStrategy implements Processor {
    public static final String SERVICE_TARGET_URL = "targetUrl";

    @Override
    public void process(Exchange exchange) throws Exception {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        ServiceOperation serviceTargetRoute = service.getServiceOperations().get(0);
        if (!serviceTargetRoute.getActive()) {
            throw new RuntimeException("Target service" + serviceTargetRoute.getOperationName() + " is inactive");
        }
        exchange.getIn().setHeader(SERVICE_TARGET_URL,
                "direct:" + RouteIdSupport.operationRouteId(serviceTargetRoute.getOperationName()));
    }
}
