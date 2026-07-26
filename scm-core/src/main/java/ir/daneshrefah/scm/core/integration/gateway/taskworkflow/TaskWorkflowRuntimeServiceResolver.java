package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.ServiceOperation;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeRoutePlan;
import ir.daneshrefah.scm.core.integration.runtime.RuntimeServicePlan;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceOperationDefinitionClassifier;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowActionKey;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowActionPlanConfig;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowActionPlanParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TaskWorkflowRuntimeServiceResolver {
    private final TaskWorkflowActionPlanParser actionPlanParser;

    public TaskWorkflowRuntimeServiceRegistry compile(
            RuntimeRoutePlan routePlan
    ) {
        if (routePlan == null) {
            throw new TaskWorkflowRouteIdentityException(
                    "TASK_WORKFLOW runtime route plan is unavailable");
        }
        Map<TaskWorkflowActionKey, RuntimeServicePlan> bindings =
                new LinkedHashMap<>();
        for (RuntimeServicePlan servicePlan : routePlan.servicePlans()) {
            if (servicePlan == null || servicePlan.service() == null
                    || servicePlan.service().getRoutingStrategy()
                    != RoutingStrategy.TASK_WORKFLOW
                    || servicePlan.service().getServiceOperations() == null) {
                continue;
            }
            for (ServiceOperation operation
                    : servicePlan.service().getServiceOperations()) {
                if (operation == null
                        || !Boolean.TRUE.equals(operation.getActive())
                        || !ServiceOperationDefinitionClassifier
                        .isActionPlan(operation)) {
                    continue;
                }
                TaskWorkflowActionPlanConfig actionPlan =
                        actionPlanParser.parse(
                                servicePlan.service(),
                                operation
                        );
                TaskWorkflowActionKey key = new TaskWorkflowActionKey(
                        actionPlan.serviceCode(),
                        actionPlan.inboundAction()
                );
                RuntimeServicePlan previous = bindings.putIfAbsent(
                        key,
                        servicePlan
                );
                if (previous != null) {
                    throw new TaskWorkflowRouteIdentityException(
                            "Duplicate active TASK_WORKFLOW action-plan binding "
                                    + "for serviceCode="
                                    + actionPlan.serviceCode()
                                    + ", inboundAction="
                                    + actionPlan.inboundAction()
                    );
                }
            }
        }
        if (bindings.isEmpty()) {
            throw new TaskWorkflowRouteIdentityException(
                    "TASK_WORKFLOW runtime route plan has no active "
                            + "DefinitionType.ACTION_PLAN bindings"
            );
        }
        return new TaskWorkflowRuntimeServiceRegistry(bindings);
    }
}
