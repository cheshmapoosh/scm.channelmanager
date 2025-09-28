package ir.daneshrefah.scm.core.integration.operation.handler;

import io.opentelemetry.api.trace.Span;
import ir.daneshrefah.scm.common.constant.log.LogAttribute;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
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
            Span span = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
            span.setAttribute(LogAttribute.MESSAGE_REQUEST.getAttributeName(),exchange.getIn() != null ? exchange.getIn().getBody(String.class) : null);
        });
    }

    private void logAfterRoute(RouteDefinition route, Operation operation){
        route.process(exchange -> {
            Span span = (Span) exchange.getProperty(Message.CURRENT_OPEN_TELEMETRY_SPAN);
            span.setAttribute(LogAttribute.MESSAGE_RESPONSE.getAttributeName(),exchange.getIn() != null ? exchange.getIn().getBody(String.class) : null);
            span.end();
        });
    }
}
