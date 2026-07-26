package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepRequestFactory;
import org.apache.camel.Exchange;

final class TaskWorkflowStepRequestFactory implements RoutingStepRequestFactory {
    private final TaskWorkflowStepType stepType;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;

    TaskWorkflowStepRequestFactory(
            TaskWorkflowStepType stepType,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator
    ) {
        this.stepType = stepType;
        this.payloadMapper = payloadMapper;
        this.transactionCoordinator = transactionCoordinator;
    }

    @Override
    public Object create(Exchange exchange, RoutingExecutionContext context) {
        switch (stepType) {
            case APPROVE_PROCESS -> transactionCoordinator.beforeApprove(exchange);
            case BUSINESS_OPERATION -> transactionCoordinator.beforeBusinessOperation(exchange);
            case COMPLETE_PROCESS -> transactionCoordinator.beforeCompleteProcess(exchange);
            default -> {
            }
        }
        return payloadMapper.toRequest(exchange, stepType, context);
    }
}
