package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;

import java.util.List;
import java.util.Objects;

public record TaskWorkflowInboundCommandConfig(
        TaskWorkflowCommand command,
        String inboundAction,
        List<TaskWorkflowInboundCommandStepConfig> steps,
        InboundChannelServiceDefinition inboundDefinition
) {
    public TaskWorkflowInboundCommandConfig {
        Objects.requireNonNull(command, "command must not be null");
        Objects.requireNonNull(inboundAction, "inboundAction must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        Objects.requireNonNull(inboundDefinition, "inboundDefinition must not be null");
    }
}
