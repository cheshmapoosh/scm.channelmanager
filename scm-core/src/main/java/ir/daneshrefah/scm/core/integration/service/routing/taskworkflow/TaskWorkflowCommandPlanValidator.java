package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskWorkflowCommandPlanValidator {
    public void validate(Service service, TaskWorkflowInboundCommandConfig config) {
        List<TaskWorkflowInboundCommandStepConfig> steps = config.steps();
        if (config.routingStrategy() == RoutingStrategy.FIRST) {
            require(steps.size() == 1, service, config, 0, "FIRST requires exactly one step");
        }
        if (config.command() == TaskWorkflowCommand.COMPLETE_TASK) {
            require(config.routingStrategy() == RoutingStrategy.FIRST
                            && steps.size() == 1
                            && steps.getFirst().role() == TaskWorkflowRole.COMPLETE_TASK,
                    service, config, 0, "task_complete requires FIRST with role COMPLETE_TASK");
        }
        if (config.command() == TaskWorkflowCommand.APPROVE_AND_EXECUTE) {
            require(config.routingStrategy() == RoutingStrategy.CHAIN_ON_APPROVE,
                    service, config, 0, "approve_and_execute requires CHAIN_ON_APPROVE");
            require(steps.size() >= 3, service, config, 0,
                    "approve_and_execute requires approve, one or more business, and complete steps");
            require(steps.getFirst().role() == TaskWorkflowRole.APPROVE_PROCESS,
                    service, config, 0, "first role must be APPROVE_PROCESS");
            require(steps.getLast().role() == TaskWorkflowRole.COMPLETE_PROCESS,
                    service, config, steps.size() - 1, "last role must be COMPLETE_PROCESS");
            for (int i = 1; i < steps.size() - 1; i++) {
                require(steps.get(i).role() == TaskWorkflowRole.BUSINESS_OPERATION,
                        service, config, i, "middle roles must be BUSINESS_OPERATION");
            }
        } else {
            for (int i = 0; i < steps.size(); i++) {
                require(steps.get(i).role() != TaskWorkflowRole.COMPLETE_PROCESS,
                        service, config, i, "COMPLETE_PROCESS cannot be a direct inbound action");
            }
        }
    }

    private void require(boolean condition, Service service,
                         TaskWorkflowInboundCommandConfig config, int index, String reason) {
        if (condition) return;
        TaskWorkflowInboundCommandStepConfig step = index >= 0 && index < config.steps().size()
                ? config.steps().get(index) : null;
        throw new IllegalStateException("Invalid TASK_WORKFLOW command plan serviceCode="
                + (service == null ? "<null>" : service.getCode())
                + ", inboundAction=" + config.inboundAction()
                + ", routingStrategy=" + config.routingStrategy()
                + ", stepIndex=" + index + ", role=" + (step == null ? null : step.role())
                + ", operationName=" + (step == null ? null : step.operationName())
                + ", reason=" + reason);
    }
}
