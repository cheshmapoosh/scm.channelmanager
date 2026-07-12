package ir.daneshrefah.scm.core.integration.operation.handler;

import ir.daneshrefah.scm.common.model.operation.Operation;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import org.apache.camel.model.RouteDefinition;


public interface OperationTypeHandler {
    OperationType getOperationType();
    void config(RouteDefinition route, Operation operation);
}
