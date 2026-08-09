package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Owns the request shapes understood by {@code scm-task} operations.
 */
public class ProviderTaskWorkflowRequestFactory
        implements TaskWorkflowProviderRequestFactory {

    private static final Set<TaskWorkflowStepType> SUPPORTED =
            EnumSet.of(
                    TaskWorkflowStepType.START_PROCESS,
                    TaskWorkflowStepType.APPROVE_PROCESS,
                    TaskWorkflowStepType.COMPLETE_PROCESS,
                    TaskWorkflowStepType.REJECT_PROCESS,
                    TaskWorkflowStepType.TASK_COMPLETE,
                    TaskWorkflowStepType.GET_ALL_PROCESS,
                    TaskWorkflowStepType.GET_ALL_TASK,
                    TaskWorkflowStepType.GET_TASK,
                    TaskWorkflowStepType.UPDATE_DESCRIPTION
            );

    private final ObjectMapper objectMapper;

    public ProviderTaskWorkflowRequestFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(TaskWorkflowStepType stepType) {
        return stepType != null && SUPPORTED.contains(stepType);
    }

    @Override
    public Object create(
            TaskWorkflowStepType stepType,
            TaskWorkflowProviderRequestContext context
    ) {
        if (!supports(stepType)) {
            throw new IllegalArgumentException(
                    "Task provider request factory does not support stepType="
                            + stepType
            );
        }
        ObjectNode request = objectPayload(context.inputPayload(), stepType);
        return switch (stepType) {
            case START_PROCESS, GET_ALL_PROCESS , GET_ALL_TASK
//                 FIND_PROCUREMENT_BY_ACCOUNT,
//                 FIND_PROCUREMENT_BY_NATIONAL,
//                 PROCUREMENT_STATEMENT_INQUIRY
                    -> request;
            case TASK_COMPLETE -> withRequired(
                    request,
                    "taskId",
                    context.taskId(),
                    stepType
            );
            case REJECT_PROCESS, UPDATE_DESCRIPTION -> withRequired(
                    request,
                    "id",
                    context.processId(),
                    stepType
            );
            case GET_TASK -> withRequired(
                    request,
                    "processId",
                    context.processId(),
                    stepType
            );
            case APPROVE_PROCESS -> approve(request, context);
            case COMPLETE_PROCESS -> complete(context);
            case BUSINESS_OPERATION -> throw new IllegalArgumentException(
                    "BUSINESS_OPERATION is not a task-provider request"
            );
        };
    }

    private ObjectNode approve(
            ObjectNode request,
            TaskWorkflowProviderRequestContext context
    ) {
        withRequired(
                request,
                "id",
                context.processId(),
                TaskWorkflowStepType.APPROVE_PROCESS
        );
        if (!request.hasNonNull("correlationId")
                && context.processCorrelationId() != null) {
            request.put("correlationId", context.processCorrelationId());
        }
        return request;
    }

    private ObjectNode complete(
            TaskWorkflowProviderRequestContext context
    ) {
        ObjectNode request = objectMapper.createObjectNode();
        withRequired(
                request,
                "id",
                context.processId(),
                TaskWorkflowStepType.COMPLETE_PROCESS
        );
        request.put("status", "COMPLETE");
        request.putObject("attribute").put("businessResult", "SUCCESS");
        return request;
    }

    private ObjectNode withRequired(
            ObjectNode request,
            String field,
            Long value,
            TaskWorkflowStepType stepType
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "TASK_WORKFLOW stepType=" + stepType
                            + " requires " + field
            );
        }
        request.put(field, value);
        return request;
    }

    private ObjectNode objectPayload(
            JsonNode input,
            TaskWorkflowStepType stepType
    ) {
        if (input == null || input.isNull() || input.isMissingNode()) {
            return objectMapper.createObjectNode();
        }
        if (!input.isObject()) {
            throw new IllegalArgumentException(
                    "TASK_WORKFLOW stepType=" + stepType
                            + " requires a JSON object payload"
            );
        }
        return ((ObjectNode) input).deepCopy();
    }
}
