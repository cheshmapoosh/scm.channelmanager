package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;

import java.util.Objects;

public record TaskWorkflowCommandPlan(
        TaskWorkflowCommand command,
        String inboundAction,
        RoutingPlan routingPlan,
        InboundChannelServiceDefinition inboundDefinition
) {
    public TaskWorkflowCommandPlan {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(inboundAction, "inboundAction must not be null");
        Objects.requireNonNull(routingPlan, "routingPlan must not be null");
        Objects.requireNonNull(inboundDefinition, "inboundDefinition must not be null");
    }
}
