package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;

import java.util.Objects;

public record TaskWorkflowCommandPlan(
        TaskWorkflowCommand command,
        String inboundAction,
        String actionPlanName,
        RoutingPlan routingPlan,
        ServiceOperation actionPlanOperation
) {
    public TaskWorkflowCommandPlan {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(inboundAction, "inboundAction must not be null");
        Objects.requireNonNull(actionPlanName, "actionPlanName must not be null");
        Objects.requireNonNull(routingPlan, "routingPlan must not be null");
        Objects.requireNonNull(actionPlanOperation, "actionPlanOperation must not be null");
    }
}
