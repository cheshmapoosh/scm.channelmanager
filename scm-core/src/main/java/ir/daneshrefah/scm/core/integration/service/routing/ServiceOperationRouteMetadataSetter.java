package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.springframework.stereotype.Component;

import static org.apache.camel.language.constant.ConstantLanguage.constant;

@Component
public class ServiceOperationRouteMetadataSetter {

    public void apply(ProcessorDefinition<?> route, ServiceOperation operation) {
        route.setProperty(Message.SERVICE_OPERATION, constant(operation));
        route.setProperty(Message.OPERATION_NAME, constant(operation.getOperationName()));
    }

    public void apply(Exchange exchange, ServiceOperation operation) {
        exchange.setProperty(Message.SERVICE_OPERATION, operation);
        exchange.setProperty(Message.OPERATION_NAME, operation.getOperationName());
    }
}
