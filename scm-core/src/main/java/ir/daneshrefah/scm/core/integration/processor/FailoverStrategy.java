package ir.daneshrefah.scm.core.integration.processor;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.runtime.RouteIdSupport;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("failoverStrategy")
public class FailoverStrategy implements Processor {

    public static final String SERVICE_TARGET_URL = "targetUrl";
    public static final String SERVICE_TARGET_INDEX = "serviceTargetIndex";
    public static final String SERVICE_TARGETS_SIZE = "serviceTargetsSize";

    @Override
    public void process(Exchange exchange) {
        Service service = exchange.getProperty(Message.SERVICE, Service.class);
        int index = exchange.getIn().getHeader(SERVICE_TARGET_INDEX, Integer.class);
        List<ServiceOperation> serviceTargetRoutes = service.getServiceOperations();

        if (index >= serviceTargetRoutes.size()) {
            throw new RuntimeException("No more serviceTargetRoutes");
        }

        ServiceOperation serviceTargetRoute = serviceTargetRoutes.get(index);
        exchange.getIn().setHeader(SERVICE_TARGET_INDEX, index + 1); // advance for next loop
        if (!serviceTargetRoute.getActive()) {
            throw new RuntimeException("Target at index " + index + " is inactive");
        }

        exchange.getIn().setHeader(SERVICE_TARGET_URL,
                "direct:" + RouteIdSupport.operationRouteId(serviceTargetRoute.getOperationName()));
    }
}
