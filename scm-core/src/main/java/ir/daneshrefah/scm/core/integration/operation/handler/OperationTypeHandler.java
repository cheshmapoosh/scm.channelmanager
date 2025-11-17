package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.logging.utils.TraceUtils;
import org.apache.camel.model.RouteDefinition;


public interface OperationTypeHandler {
    OperationType getOperationType();
    void config(RouteDefinition route, Operation operation);

    default void internalConfig(RouteDefinition route, Operation operation){
        logBeforeRoute(route, operation);
        config(route, operation);
        logAfterRoute(route, operation);
    }

    private void logBeforeRoute(RouteDefinition route, Operation operation){
        route.process(exchange -> {
            TraceUtils.getInstance().traceBeforeOperation(exchange,operation);
        });
    }

    private void logAfterRoute(RouteDefinition route, Operation operation){
        route.process(exchange -> {
           TraceUtils.getInstance().traceAfterOperation(exchange,operation);
        });
    }
}
