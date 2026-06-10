package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import org.apache.camel.model.RouteDefinition;


public interface OperationTypeHandler {
    OperationType getOperationType();
    void config(RouteDefinition route, Operation operation);

    default void internalConfig(RouteDefinition route, Operation operation, CoreObservationTraceSupport observationTraceSupport){
        startOperationCall(route, operation, observationTraceSupport);
        config(route, operation);
        finishOperationCall(route, operation, observationTraceSupport);
    }

    private void startOperationCall(RouteDefinition route, Operation operation, CoreObservationTraceSupport observationTraceSupport){
        route.process(exchange -> {
            observationTraceSupport.startOperationCall(exchange, operation);
        });
    }

    private void finishOperationCall(RouteDefinition route, Operation operation, CoreObservationTraceSupport observationTraceSupport){
        route.process(exchange -> {
           observationTraceSupport.finishOperationCallSuccess(exchange, operation);
        });
    }
}
