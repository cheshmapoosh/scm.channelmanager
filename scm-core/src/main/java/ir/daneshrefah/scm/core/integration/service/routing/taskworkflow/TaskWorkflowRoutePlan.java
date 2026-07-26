package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.Service;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record TaskWorkflowRoutePlan(
        Service service,
        Map<TaskWorkflowActionKey, TaskWorkflowCommandPlan> commandPlans
) {
    public TaskWorkflowRoutePlan {
        Objects.requireNonNull(service, "service must not be null");
        Map<TaskWorkflowActionKey, TaskWorkflowCommandPlan> copy =
                new LinkedHashMap<>();
        copy.putAll(Objects.requireNonNull(commandPlans, "commandPlans must not be null"));
        commandPlans = Map.copyOf(copy);
    }

    public TaskWorkflowCommandPlan requireCommandPlan(String inboundAction) {
        String action = StringUtils.trimToNull(inboundAction);
        if (action == null) {
            throw new IllegalStateException("TASK_WORKFLOW route identity is incomplete for "
                    + "serviceCode=" + service.getCode() + ", inboundAction="
                    + inboundAction);
        }
        TaskWorkflowCommandPlan plan = commandPlans.get(
                new TaskWorkflowActionKey(service.getCode(), action));
        if (plan == null) {
            throw new IllegalStateException("No TASK_WORKFLOW route plan for serviceCode="
                    + service.getCode() + ", inboundAction=" + inboundAction);
        }
        return plan;
    }
}
