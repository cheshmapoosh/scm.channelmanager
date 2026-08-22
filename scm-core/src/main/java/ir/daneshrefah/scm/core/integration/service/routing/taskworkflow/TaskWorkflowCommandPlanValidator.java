package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskWorkflowCommandPlanValidator {
    public void validate(Service service, TaskWorkflowActionPlanConfig config) {
        List<TaskWorkflowActionPlanStepConfig> steps = config.steps();
        if (config.routingStrategy() == RoutingStrategy.FIRST) {
            require(steps.size() == 1, service, config, 0, "FIRST requires exactly one step");
        }
        if (config.command() == TaskWorkflowCommand.APPROVE_AND_EXECUTE) {
            require(config.routingStrategy() == RoutingStrategy.CHAIN_ON_APPROVE,
                    service, config, 0, "approve_and_execute requires CHAIN_ON_APPROVE");
            require(steps.size() >= 3, service, config, 0,
                    "approve_and_execute requires approve, one or more business, and complete steps");
            require(steps.getFirst().stepType() == TaskWorkflowStepType.APPROVE_PROCESS,
                    service, config, 0, "first stepType must be APPROVE_PROCESS");
            require(steps.getLast().stepType() == TaskWorkflowStepType.COMPLETE_PROCESS,
                    service, config, steps.size() - 1, "last stepType must be COMPLETE_PROCESS");
            for (int i = 1; i < steps.size() - 1; i++) {
                require(steps.get(i).stepType() == TaskWorkflowStepType.BUSINESS_OPERATION,
                        service, config, i, "middle stepTypes must be BUSINESS_OPERATION");
            }
        } else {
            if(config.routingStrategy() == RoutingStrategy.FIRST){
                require(config.routingStrategy() == RoutingStrategy.FIRST,
                        service, config, 0, "direct actions require FIRST");
                TaskWorkflowStepType requiredStepType = requiredDirectStepType(config.command());
                require(steps.size() == 1 && steps.getFirst().stepType() == requiredStepType,
                        service, config, 0, config.inboundAction()
                                + " requires stepType " + requiredStepType);
            }else if(config.routingStrategy() == RoutingStrategy.CHAIN_ON_APPROVE){
                for (int i = 1; i < steps.size() - 1; i++) {
                    require(steps.get(i).stepType() == TaskWorkflowStepType.BUSINESS_OPERATION,
                            service, config, i, "middle stepTypes must be BUSINESS_OPERATION");
                }
            }
        }
    }

    private TaskWorkflowStepType requiredDirectStepType(TaskWorkflowCommand command) {
        return switch (command) {
            case START_PROCESS -> TaskWorkflowStepType.START_PROCESS;
            case TASK_COMPLETE -> TaskWorkflowStepType.TASK_COMPLETE;
            case REJECT_PROCESS -> TaskWorkflowStepType.REJECT_PROCESS;
            case GET_ALL_PROCESS -> TaskWorkflowStepType.GET_ALL_PROCESS;
            case GET_ALL_TASK -> TaskWorkflowStepType.GET_ALL_TASK;
            case GET_TASK -> TaskWorkflowStepType.GET_TASK;
            case FIND_PROCUREMENT_BY_ACCOUNT -> TaskWorkflowStepType.BUSINESS_OPERATION;//FIND_PROCUREMENT_BY_ACCOUNT;
            case FIND_PROCUREMENT_BY_NATIONAL -> TaskWorkflowStepType.BUSINESS_OPERATION;//FIND_PROCUREMENT_BY_NATIONAL;
            case PROCUREMENT_STATEMENT_INQUIRY -> TaskWorkflowStepType.BUSINESS_OPERATION;//PROCUREMENT_STATEMENT_INQUIRY;
            case WITHDRAW -> TaskWorkflowStepType.BUSINESS_OPERATION;
            case UPDATE_DESCRIPTION ->
                    TaskWorkflowStepType.UPDATE_DESCRIPTION;
            case APPROVE_AND_EXECUTE,DELETE_PROCUREMENT -> throw new IllegalArgumentException(
                    "APPROVE_AND_EXECUTE is not a direct action");
        };
    }

    private void require(boolean condition, Service service,
                         TaskWorkflowActionPlanConfig config, int index, String reason) {
        if (condition) return;
        TaskWorkflowActionPlanStepConfig step = index >= 0 && index < config.steps().size()
                ? config.steps().get(index) : null;
        throw new IllegalStateException("Invalid TASK_WORKFLOW command plan serviceCode="
                + (service == null ? "<null>" : service.getCode())
                + ", inboundAction=" + config.inboundAction()
                + ", actionPlanName=" + config.actionPlanName()
                + ", definitionId=" + config.definitionId()
                + ", routingStrategy=" + config.routingStrategy()
                + ", stepId=" + (step == null ? null : step.stepId())
                + ", stepIndex=" + index + ", stepType=" + (step == null ? null : step.stepType())
                + ", operationName=" + (step == null ? null : step.operationName())
                + ", reason=" + reason);
    }
}
