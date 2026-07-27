package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads protocol-neutral, already normalized request identities. Authoritative
 * server execution IDs are selected by the execution coordinator only after
 * START/new-versus-existing resolution.
 */
@Component
public class TaskWorkflowExecutionIdentityResolver {
    private static final int MAX_EXECUTION_ID_LENGTH = 100;

    public ResolvedRequestIdentity resolve(Exchange exchange) {
        Map<String, String> executionIds = new LinkedHashMap<>();
        addExecutionId(
                executionIds,
                "request decoder",
                exchange.getProperty(Message.EXECUTION_ID, String.class),
                false
        );
        addExecutionId(
                executionIds,
                "normalized payload",
                payloadExecutionId(exchange),
                true
        );
        addExecutionId(
                executionIds,
                "inbound parameters",
                parameterValue(exchange, "executionId"),
                true
        );

        String suppliedExecutionId = oneValue(
                executionIds,
                "executionId"
        );
        Map<String, String> clientCorrelations = new LinkedHashMap<>();
        addClientCorrelation(
                clientCorrelations,
                "request decoder",
                exchange.getProperty(
                        Message.CLIENT_CORRELATION_ID,
                        String.class
                ),
                false
        );
        addClientCorrelation(
                clientCorrelations,
                "inbound parameters",
                firstParameterValue(
                        exchange,
                        "scmClientCorrelationId",
                        "clientCorrelationId"
                ),
                true
        );
        String clientCorrelation = oneValue(
                clientCorrelations,
                "scmClientCorrelationId"
        );
        return new ResolvedRequestIdentity(
                suppliedExecutionId,
                clientCorrelation
        );
    }

    private String payloadExecutionId(Exchange exchange) {
        Message normalized = exchange.getProperty(
                Message.INTERNAL_MESSAGE,
                Message.class
        );
        JsonNode payload = normalized == null ? null : normalized.getPayload();
        if (payload == null || !payload.isObject()
                || !payload.has("executionId")
                || payload.get("executionId").isNull()) {
            return null;
        }
        JsonNode value = payload.get("executionId");
        if (!value.isTextual()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW payload executionId must be a string"
            );
        }
        return value.textValue();
    }

    private String firstParameterValue(
            Exchange exchange,
            String... names
    ) {
        String resolved = null;
        for (String name : names) {
            String candidate = parameterValue(exchange, name);
            if (candidate == null) {
                continue;
            }
            if (resolved != null && !resolved.equals(candidate)) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "Conflicting TASK_WORKFLOW client-correlation values "
                                + "in inbound parameters"
                );
            }
            resolved = candidate;
        }
        return resolved;
    }

    private String parameterValue(Exchange exchange, String requestedName) {
        Object value = exchange.getProperty(Message.INBOUND_PARAMETERS);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?> parameters)) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW inbound parameters must be a map"
            );
        }
        String resolved = null;
        for (Map.Entry<?, ?> entry : parameters.entrySet()) {
            if (!(entry.getKey() instanceof String key)
                    || !key.equalsIgnoreCase(requestedName)) {
                continue;
            }
            String candidate = entry.getValue() == null
                    ? null
                    : String.valueOf(entry.getValue());
            if (resolved != null && !resolved.equals(candidate)) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "Conflicting TASK_WORKFLOW " + requestedName
                                + " values in inbound parameters"
                );
            }
            resolved = candidate;
        }
        return resolved;
    }

    private void addExecutionId(
            Map<String, String> values,
            String source,
            String value,
            boolean rejectBlank
    ) {
        String normalized = normalize(value, source, rejectBlank);
        if (normalized == null) {
            return;
        }
        if (normalized.length() > MAX_EXECUTION_ID_LENGTH) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW executionId from " + source
                            + " exceeds " + MAX_EXECUTION_ID_LENGTH
                            + " characters"
            );
        }
        values.put(source, normalized);
    }

    private void addClientCorrelation(
            Map<String, String> values,
            String source,
            String value,
            boolean rejectBlank
    ) {
        String normalized = normalize(value, source, rejectBlank);
        if (normalized != null) {
            values.put(source, normalized);
        }
    }

    private String normalize(
            String value,
            String source,
            boolean rejectBlank
    ) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null && rejectBlank && value != null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW identity from " + source
                            + " must not be blank"
            );
        }
        return normalized;
    }

    private String oneValue(
            Map<String, String> values,
            String identityName
    ) {
        String resolved = null;
        String source = null;
        for (Map.Entry<String, String> candidate : values.entrySet()) {
            if (resolved != null && !resolved.equals(candidate.getValue())) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "Conflicting TASK_WORKFLOW " + identityName
                                + " values from " + source + " and "
                                + candidate.getKey()
                );
            }
            resolved = candidate.getValue();
            source = candidate.getKey();
        }
        return resolved;
    }

    public record ResolvedRequestIdentity(
            String suppliedExecutionId,
            String scmClientCorrelationId
    ) {
    }
}
