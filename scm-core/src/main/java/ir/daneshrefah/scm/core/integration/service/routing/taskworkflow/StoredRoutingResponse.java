package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.ExecutionOutcome;

public record StoredRoutingResponse(
        MessageStatus status,
        JsonNode payload,
        ExecutionOutcome executionOutcome
) {
    public StoredRoutingResponse {
        payload = payload == null ? null : payload.deepCopy();
    }
}
