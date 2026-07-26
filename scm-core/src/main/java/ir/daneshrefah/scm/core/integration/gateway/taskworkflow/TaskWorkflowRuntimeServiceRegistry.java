package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowActionKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record TaskWorkflowRuntimeServiceRegistry(
        Map<TaskWorkflowActionKey, RuntimeServicePlan> servicePlans
) {
    public TaskWorkflowRuntimeServiceRegistry {
        Map<TaskWorkflowActionKey, RuntimeServicePlan> copy =
                new LinkedHashMap<>();
        copy.putAll(Objects.requireNonNull(
                servicePlans,
                "servicePlans must not be null"
        ));
        servicePlans = Map.copyOf(copy);
    }

    public RuntimeServicePlan resolve(TaskWorkflowRouteIdentity identity) {
        RuntimeServicePlan servicePlan = servicePlans.get(
                new TaskWorkflowActionKey(
                        identity.serviceCode(),
                        identity.inboundAction()
                )
        );
        if (servicePlan == null) {
            throw new TaskWorkflowRouteIdentityException(
                    "No active TASK_WORKFLOW action-plan binding for serviceCode="
                            + identity.serviceCode() + ", inboundAction="
                            + identity.inboundAction()
            );
        }
        return servicePlan;
    }
}
