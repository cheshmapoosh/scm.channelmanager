package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Minimal, sanitized state required to reconstruct a later workflow step.
 */
public record TaskWorkflowResumeData(
        Long processId,
        String correlationId,
        JsonNode transactionData,
        JsonNode retryRequest,
        JsonNode lastBusinessResponse,
        String providerIdempotencyKey
) {
    public TaskWorkflowResumeData {
        transactionData = copy(transactionData);
        retryRequest = copy(retryRequest);
        lastBusinessResponse = copy(lastBusinessResponse);
    }

    private static JsonNode copy(JsonNode value) {
        return value == null ? null : value.deepCopy();
    }
}
