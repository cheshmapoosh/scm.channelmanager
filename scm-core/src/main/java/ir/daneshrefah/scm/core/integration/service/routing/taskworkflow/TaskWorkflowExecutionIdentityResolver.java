package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.Message;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

@Component
public class TaskWorkflowExecutionIdentityResolver {
    public static final String EXECUTION_ID_HEADER = "X-SCM-Execution-ID";
    private static final int MAX_EXECUTION_ID_LENGTH = 100;

    public ResolvedExecutionIdentity resolve(
            Exchange exchange,
            TaskWorkflowCommand command
    ) {
        Map<String, String> supplied = new LinkedHashMap<>();
        add(supplied, "request decoder",
                exchange.getProperty(Message.EXECUTION_ID, String.class), false);
        add(supplied, "normalized payload", payloadExecutionId(exchange), true);
        add(supplied, "inbound parameters", parameterExecutionId(exchange), true);
        add(supplied, EXECUTION_ID_HEADER,
                exchange.getMessage().getHeader(
                        EXECUTION_ID_HEADER, String.class), false);

        String executionId = null;
        String source = null;
        for (Map.Entry<String, String> candidate : supplied.entrySet()) {
            if (executionId != null
                    && !executionId.equals(candidate.getValue())) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "Conflicting TASK_WORKFLOW executionId values from "
                                + source + " and " + candidate.getKey());
            }
            executionId = candidate.getValue();
            source = candidate.getKey();
        }

        boolean explicitlySupplied = executionId != null;
        if (executionId == null && command == TaskWorkflowCommand.START) {
            executionId = normalize(
                    exchange.getMessage().getHeader(
                            SCM_PARAMETER_CLIENT_CORRELATION_ID,
                            String.class
                    ),
                    SCM_PARAMETER_CLIENT_CORRELATION_ID,
                    false
            );
            explicitlySupplied = executionId != null;
        }
        if (executionId == null && command == TaskWorkflowCommand.START) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW start requires a canonical client executionId");
        }
        if (executionId == null) {
            executionId = UUID.randomUUID().toString();
        }
        exchange.setProperty(Message.EXECUTION_ID, executionId);
        return new ResolvedExecutionIdentity(executionId, explicitlySupplied);
    }

    private String payloadExecutionId(Exchange exchange) {
        Message normalized = exchange.getProperty(
                Message.INTERNAL_MESSAGE, Message.class);
        JsonNode payload = normalized == null ? null : normalized.getPayload();
        if (payload == null || !payload.isObject()
                || !payload.has("executionId")
                || payload.get("executionId").isNull()) {
            return null;
        }
        JsonNode value = payload.get("executionId");
        if (!value.isTextual()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW payload executionId must be a string");
        }
        return normalize(value.textValue(), "normalized payload", true);
    }

    private String parameterExecutionId(Exchange exchange) {
        Object value = exchange.getProperty(Message.INBOUND_PARAMETERS);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?> parameters)) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW inbound parameters must be a map");
        }
        String resolved = null;
        for (Map.Entry<?, ?> entry : parameters.entrySet()) {
            if (!(entry.getKey() instanceof String key)
                    || !key.equalsIgnoreCase("executionId")) {
                continue;
            }
            String candidate = normalize(
                    entry.getValue() == null
                            ? null
                            : String.valueOf(entry.getValue()),
                    "inbound parameters",
                    true
            );
            if (resolved != null && !resolved.equals(candidate)) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "Conflicting TASK_WORKFLOW executionId values in inbound parameters");
            }
            resolved = candidate;
        }
        return resolved;
    }

    private void add(
            Map<String, String> supplied,
            String source,
            String value,
            boolean rejectBlank
    ) {
        String normalized = normalize(value, source, rejectBlank);
        if (normalized != null) {
            supplied.put(source, normalized);
        }
    }

    private String normalize(
            String value,
            String source,
            boolean rejectBlank
    ) {
        String normalized = StringUtils.trimToNull(value);
        if (normalized == null) {
            if (rejectBlank && value != null) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "TASK_WORKFLOW executionId from " + source
                                + " must not be blank");
            }
            return null;
        }
        if (normalized.length() > MAX_EXECUTION_ID_LENGTH) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW executionId from " + source
                            + " exceeds " + MAX_EXECUTION_ID_LENGTH + " characters");
        }
        return normalized;
    }

    public record ResolvedExecutionIdentity(
            String executionId,
            boolean explicitlySupplied
    ) {
    }
}
