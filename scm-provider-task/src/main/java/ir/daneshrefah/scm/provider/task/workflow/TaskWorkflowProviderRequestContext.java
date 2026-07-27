package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Provider-neutral values required to format task-provider operation requests.
 */
public record TaskWorkflowProviderRequestContext(
        JsonNode inputPayload,
        Long processId,
        Long taskId,
        String processCorrelationId
) {
    public TaskWorkflowProviderRequestContext {
        inputPayload = inputPayload == null ? null : inputPayload.deepCopy();
    }
}
