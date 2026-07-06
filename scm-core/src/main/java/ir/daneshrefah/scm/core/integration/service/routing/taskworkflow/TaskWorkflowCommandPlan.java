package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.InboundChannelServiceDefinition;

import java.util.List;
import java.util.Objects;

public record TaskWorkflowCommandPlan(
        TaskWorkflowCommand command,
        List<TaskWorkflowStepPlan> steps,
        InboundChannelServiceDefinition inboundDefinition
) {
    public TaskWorkflowCommandPlan {
        Objects.requireNonNull(command, "command must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        Objects.requireNonNull(inboundDefinition, "inboundDefinition must not be null");
    }

    public TaskWorkflowStepPlan requireRole(TaskWorkflowRole role) {
        return steps.stream()
                .filter(step -> step.role() == role)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("TASK_WORKFLOW command="
                        + command + " has no step for role=" + role));
    }
}
