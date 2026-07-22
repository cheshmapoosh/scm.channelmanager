package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepRequestFactory;
import org.apache.camel.Exchange;

final class TaskWorkflowStepRequestFactory implements RoutingStepRequestFactory {
    private final TaskWorkflowRole role;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;

    TaskWorkflowStepRequestFactory(
            TaskWorkflowRole role,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator
    ) {
        this.role = role;
        this.payloadMapper = payloadMapper;
        this.transactionCoordinator = transactionCoordinator;
    }

    @Override
    public Object create(Exchange exchange, RoutingExecutionContext context) {
        switch (role) {
            case APPROVE_PROCESS -> transactionCoordinator.beforeApprove(exchange);
            case BUSINESS_OPERATION -> transactionCoordinator.beforeBusinessOperation(exchange);
            case COMPLETE_PROCESS -> transactionCoordinator.beforeCompleteProcess(exchange);
            default -> {
            }
        }
        return payloadMapper.toRequest(exchange, role, context);
    }
}
