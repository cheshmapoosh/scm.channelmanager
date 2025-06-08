package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

@Component
public class BeanOperationTypeHandler implements OperationTypeHandler {
    @Override
    public OperationType getOperationType() {
        return OperationType.BEAN;
    }

    @Override
    public void config(RouteDefinition route, Operation operation) {
        route.to("bean:" + operation.getPath());
    }
}
