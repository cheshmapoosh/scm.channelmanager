package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.Objects;

@Component
public class TaskWorkflowInputResolver {
    private static final String NORMALIZED_PAYLOAD = "normalized payload";
    private static final String INBOUND_PARAMETERS = "inbound parameters";

    private final ObjectMapper objectMapper;

    public TaskWorkflowInputResolver(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Long requireTaskId(Exchange exchange, TaskWorkflowStepType stepType) {
        Long taskId = resolveTaskId(exchange, stepType);
        if (taskId == null) {
            throw required(stepType, "taskId");
        }
        return taskId;
    }

    public Long requireProcessId(Exchange exchange, TaskWorkflowStepType stepType) {
        Long processId = resolveProcessId(exchange, stepType);
        if (processId == null) {
            throw required(stepType, "processId");
        }
        return processId;
    }

    public Long resolveTaskId(Exchange exchange, TaskWorkflowStepType stepType) {
        return resolve(exchange, stepType, "taskId", "taskId");
    }

    public Long resolveProcessId(Exchange exchange, TaskWorkflowStepType stepType) {
        return resolve(exchange, stepType, "processId", "processId", "id");
    }

    private Long resolve(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            String identifier,
            String... payloadNames
    ) {
        Objects.requireNonNull(exchange, "exchange must not be null");
        Objects.requireNonNull(stepType, "stepType must not be null");
        Long payloadValue = payloadIdentifier(exchange, stepType, identifier, payloadNames);
        Long parameterValue = inboundParameter(exchange, stepType, identifier);
        if (payloadValue != null && parameterValue != null
                && !payloadValue.equals(parameterValue)) {
            throw new IllegalStateException("Conflicting TASK_WORKFLOW " + identifier
                    + " values were supplied by normalized payload and inbound parameters");
        }
        return payloadValue != null ? payloadValue : parameterValue;
    }

    private Long payloadIdentifier(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            String identifier,
            String... names
    ) {
        JsonNode payload = normalizedPayload(exchange);
        if (payload == null || payload.isNull() || payload.isMissingNode() || !payload.isObject()) {
            return null;
        }
        Long resolved = null;
        String resolvedName = null;
        var fields = payload.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (!matches(field.getKey(), names) || absent(field.getValue())) {
                continue;
            }
            Long candidate = identifierValue(
                    field.getValue(),
                    stepType,
                    identifier,
                    NORMALIZED_PAYLOAD
            );
            if (resolved != null && !resolved.equals(candidate)) {
                throw new IllegalStateException("Conflicting TASK_WORKFLOW " + identifier
                        + " values were supplied by normalized payload fields "
                        + resolvedName + " and " + field.getKey());
            }
            resolved = candidate;
            resolvedName = field.getKey();
        }
        return resolved;
    }

    private Long inboundParameter(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            String identifier
    ) {
        Object value = exchange.getProperty(Message.INBOUND_PARAMETERS);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Map<?, ?> parameters)) {
            throw new IllegalStateException("TASK_WORKFLOW exchange property "
                    + Message.INBOUND_PARAMETERS + " must be a Map<String, Object>");
        }
        Long resolved = null;
        String resolvedName = null;
        for (Map.Entry<?, ?> entry : parameters.entrySet()) {
            if (!(entry.getKey() instanceof String name)) {
                throw new IllegalStateException("TASK_WORKFLOW exchange property "
                        + Message.INBOUND_PARAMETERS + " must be a Map<String, Object>");
            }
            if (!name.equalsIgnoreCase(identifier) || absent(entry.getValue())) {
                continue;
            }
            Long candidate = identifierValue(
                    entry.getValue(),
                    stepType,
                    identifier,
                    INBOUND_PARAMETERS
            );
            if (resolved != null && !resolved.equals(candidate)) {
                throw new IllegalStateException("Conflicting TASK_WORKFLOW " + identifier
                        + " values were supplied by inbound parameters "
                        + resolvedName + " and " + name);
            }
            resolved = candidate;
            resolvedName = name;
        }
        return resolved;
    }

    private JsonNode normalizedPayload(Exchange exchange) {
        Object normalized = exchange.getProperty(Message.INTERNAL_MESSAGE);
        if (normalized instanceof Message message) {
            return message.getPayload();
        }
        if (normalized != null) {
            return toJsonNode(normalized);
        }
        Object original = exchange.getProperty(Message.ORIGINAL_BODY);
        if (original != null) {
            return toJsonNode(original);
        }
        Object body = exchange.getMessage().getBody();
        if (body instanceof Message message) {
            return message.getPayload();
        }
        return toJsonNode(body);
    }

    private JsonNode toJsonNode(Object value) {
        if (value instanceof JsonNode jsonNode) {
            return jsonNode;
        }
        return value == null ? null : objectMapper.valueToTree(value);
    }

    private Long identifierValue(
            Object value,
            TaskWorkflowStepType stepType,
            String identifier,
            String source
    ) {
        try {
            if (value instanceof JsonNode node) {
                if (node.isIntegralNumber()) {
                    if (!node.canConvertToLong()) {
                        throw new ArithmeticException("outside long range");
                    }
                    return node.longValue();
                }
                if (node.isFloatingPointNumber()) {
                    return node.decimalValue().longValueExact();
                }
                if (node.isTextual()) {
                    return numericText(node.textValue());
                }
                throw new IllegalArgumentException("unsupported JSON value");
            }
            if (value instanceof BigInteger number) {
                return number.longValueExact();
            }
            if (value instanceof BigDecimal number) {
                return number.longValueExact();
            }
            if (value instanceof Byte || value instanceof Short
                    || value instanceof Integer || value instanceof Long) {
                return ((Number) value).longValue();
            }
            if (value instanceof Number number) {
                return new BigDecimal(number.toString()).longValueExact();
            }
            if (value instanceof String text) {
                return numericText(text);
            }
            throw new IllegalArgumentException("unsupported value");
        } catch (ArithmeticException | NumberFormatException exception) {
            throw invalid(stepType, identifier, source, exception);
        } catch (IllegalArgumentException exception) {
            throw invalid(stepType, identifier, source, exception);
        }
    }

    private Long numericText(String value) {
        if (value == null || value.isBlank()) {
            throw new NumberFormatException("blank value");
        }
        return Long.valueOf(value.trim());
    }

    private boolean absent(Object value) {
        return value == null || value instanceof JsonNode node
                && (node.isNull() || node.isMissingNode());
    }

    private boolean matches(String field, String... names) {
        for (String name : names) {
            if (field.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private IllegalStateException required(
            TaskWorkflowStepType stepType,
            String identifier
    ) {
        return new IllegalStateException("TASK_WORKFLOW stepType=" + stepType
                + " requires " + identifier);
    }

    private IllegalStateException invalid(
            TaskWorkflowStepType stepType,
            String identifier,
            String source,
            RuntimeException cause
    ) {
        return new IllegalStateException("Invalid TASK_WORKFLOW " + identifier
                + " value from " + source + " for stepType=" + stepType
                + "; expected an integral number or numeric string", cause);
    }
}
