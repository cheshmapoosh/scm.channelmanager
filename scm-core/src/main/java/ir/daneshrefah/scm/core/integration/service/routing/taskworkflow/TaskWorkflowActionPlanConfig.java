package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;

import java.util.List;
import java.util.Objects;

public record TaskWorkflowActionPlanConfig(
        TaskWorkflowCommand command,
        String serviceCode,
        String inboundAction,
        String actionPlanName,
        String sourceServiceOperationId,
        String definitionId,
        RoutingStrategy routingStrategy,
        List<TaskWorkflowActionPlanStepConfig> steps,
        ServiceOperation actionPlanOperation
) {
    public TaskWorkflowActionPlanConfig {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(serviceCode, "serviceCode must not be null");
        Objects.requireNonNull(inboundAction, "inboundAction must not be null");
        Objects.requireNonNull(actionPlanName, "actionPlanName must not be null");
        Objects.requireNonNull(
                sourceServiceOperationId,
                "sourceServiceOperationId must not be null"
        );
        Objects.requireNonNull(definitionId, "definitionId must not be null");
        Objects.requireNonNull(routingStrategy, "routingStrategy must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        Objects.requireNonNull(actionPlanOperation, "actionPlanOperation must not be null");
    }
}
