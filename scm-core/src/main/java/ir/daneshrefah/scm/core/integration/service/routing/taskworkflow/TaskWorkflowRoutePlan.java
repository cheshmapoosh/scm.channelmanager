package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record TaskWorkflowRoutePlan(
        Service service,
        Map<TaskWorkflowCommand, TaskWorkflowCommandPlan> commandPlans
) {
    public TaskWorkflowRoutePlan {
        Objects.requireNonNull(service, "service must not be null");
        EnumMap<TaskWorkflowCommand, TaskWorkflowCommandPlan> copy =
                new EnumMap<>(TaskWorkflowCommand.class);
        copy.putAll(Objects.requireNonNull(commandPlans, "commandPlans must not be null"));
        commandPlans = Map.copyOf(copy);
    }

    public TaskWorkflowCommandPlan requireCommandPlan(TaskWorkflowCommand command) {
        TaskWorkflowCommandPlan plan = commandPlans.get(command);
        if (plan == null) {
            throw new IllegalStateException("No TASK_WORKFLOW route plan for serviceCode="
                    + service.getCode() + ", command=" + command);
        }
        return plan;
    }
}
