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
                    TaskWorkflowStepType.CANCEL_PROCESS,
                    TaskWorkflowStepType.COMPLETE_TASK,
                    TaskWorkflowStepType.FIND_ALL_PROCESS,
                    TaskWorkflowStepType.FIND_ALL_TASK,
                    TaskWorkflowStepType.FIND_TASK_BY_PROCESS_ID,
                    TaskWorkflowStepType.UPDATE_PROCESS_DESCRIPTION
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
            case START_PROCESS, FIND_ALL_PROCESS, FIND_ALL_TASK -> request;
            case COMPLETE_TASK -> withRequired(
                    request,
                    "taskId",
                    context.taskId(),
                    stepType
            );
            case CANCEL_PROCESS, UPDATE_PROCESS_DESCRIPTION -> withRequired(
                    request,
                    "id",
                    context.processId(),
                    stepType
            );
            case FIND_TASK_BY_PROCESS_ID -> withRequired(
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
