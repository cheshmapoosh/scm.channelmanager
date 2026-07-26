package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.ExecutionOutcome;

public record TaskWorkflowStoredResponse(
        MessageStatus status,
        JsonNode payload,
        ExecutionOutcome executionOutcome
) {
    public TaskWorkflowStoredResponse {
        payload = payload == null ? null : payload.deepCopy();
    }
}
