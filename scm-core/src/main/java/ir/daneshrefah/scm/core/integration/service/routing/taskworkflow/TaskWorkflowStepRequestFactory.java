package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepRequestFactory;
import org.apache.camel.Exchange;

final class TaskWorkflowStepRequestFactory implements RoutingStepRequestFactory {
    private final String stepId;
    private final TaskWorkflowStepType stepType;
    private final TaskWorkflowPayloadMapper payloadMapper;

    TaskWorkflowStepRequestFactory(
            String stepId,
            TaskWorkflowStepType stepType,
            TaskWorkflowPayloadMapper payloadMapper
    ) {
        this.stepId = stepId;
        this.stepType = stepType;
        this.payloadMapper = payloadMapper;
    }

    @Override
    public Object create(Exchange exchange, RoutingExecutionContext context) {
        Object request = payloadMapper.toRequest(
                exchange,
                stepId,
                stepType,
                context
        );
        exchange.setProperty(
                TaskWorkflowExchangeProperties.CURRENT_STEP_REQUEST,
                request
        );
        return request;
    }
}
