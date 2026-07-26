package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;

public record RoutingRecoveryContext(
        String correlationId,
        JsonNode transactionData,
        Map<String, JsonNode> stepResults
) {
    public RoutingRecoveryContext {
        transactionData = transactionData == null
                ? null
                : transactionData.deepCopy();
        Map<String, JsonNode> copied = new LinkedHashMap<>();
        if (stepResults != null) {
            stepResults.forEach((key, value) ->
                    copied.put(key, value == null ? null : value.deepCopy()));
        }
        stepResults = Map.copyOf(copied);
    }
}
