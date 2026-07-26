package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepRequestFactory;
import org.apache.camel.Exchange;

final class TaskWorkflowStepRequestFactory implements RoutingStepRequestFactory {
    private final TaskWorkflowStepType stepType;
    private final TaskWorkflowPayloadMapper payloadMapper;

    TaskWorkflowStepRequestFactory(
            TaskWorkflowStepType stepType,
            TaskWorkflowPayloadMapper payloadMapper
    ) {
        this.stepType = stepType;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public Object create(Exchange exchange, RoutingExecutionContext context) {
        return payloadMapper.toRequest(exchange, stepType, context);
    }
}
