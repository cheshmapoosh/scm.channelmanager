package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.Map;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

@Component
public class TaskWorkflowPayloadMapper {
    private final ObjectMapper objectMapper;

    public TaskWorkflowPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object toRequest(
            Exchange exchange,
            TaskWorkflowRole role,
            RoutingExecutionContext context
    ) {
        return switch (role) {
            case APPROVE_PROCESS -> toApproveRequest(exchange);
            case BUSINESS_OPERATION -> toBusinessRequest(exchange, context);
            case COMPLETE_PROCESS -> toCompleteProcessRequest(exchange, context);
            default -> toSimpleRequest(exchange, role);
        };
    }

    private ObjectNode toBusinessRequest(Exchange exchange, RoutingExecutionContext context) {
        Long processId = context.processId();
        if (processId == null) {
            processId = exchange.getProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    Long.class
            );
        }
        if (processId == null) processId = pathLong(exchange, "processId");
        context.processId(processId);
        if (processId != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);
        }
        String correlationId = correlationId(exchange);
        context.correlationId(correlationId);
        if (correlationId != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.CORRELATION_ID, correlationId);
        }
        Object stableTransactionData = context.transactionData();
        if (stableTransactionData == null) {
            stableTransactionData = objectMapper.createObjectNode();
        }
        context.transactionData(stableTransactionData);
        ObjectNode request = objectMapper.createObjectNode();
        if (processId != null) request.put("processId", processId);
        if (correlationId != null) request.put("correlationId", correlationId);
        request.set("transactionData", toJsonNode(stableTransactionData));
        request.set("stepResults", objectMapper.valueToTree(context.stepResults()));
        return request;
    }

    public void rememberApproveContext(
            Object response,
            RoutingExecutionContext context
    ) {
        JsonNode approveResponse = toJsonNode(response);
        Long processId = longValue(approveResponse.path("id"));
        if (processId == null) {
            processId = longValue(approveResponse.path("processId"));
        }
        if (processId != null) {
            context.processId(processId);
        }
        JsonNode transactionData = approveResponse.get("transactionData");
        context.transactionData(transactionData == null || transactionData.isNull()
                ? approveResponse
                : transactionData.deepCopy());
    }

    public JsonNode toSimpleRequest(Exchange exchange, TaskWorkflowRole role) {
        ObjectNode request = objectRequest(exchange, role);
        return switch (role) {
            case START_PROCESS, FIND_ALL_PROCESS, FIND_ALL_TASK -> request;
            case COMPLETE_TASK -> withTaskId(exchange, request);
            case CANCEL_PROCESS, UPDATE_PROCESS_DESCRIPTION -> withProcessId(exchange, request);
            case FIND_TASK_BY_PROCESS_ID -> findTasksByProcessId(exchange, request);
            case APPROVE_PROCESS, BUSINESS_OPERATION, COMPLETE_PROCESS -> throw new IllegalStateException("Role " + role
                    + " requires the coordinated APPROVE_AND_EXECUTE flow");
        };
    }

    public ObjectNode toApproveRequest(Exchange exchange) {
        ObjectNode request = objectRequest(exchange, TaskWorkflowRole.APPROVE_PROCESS);
        Long processId = pathLong(exchange, "processId");
        if (processId != null) {
            request.put("id", processId);
        }
        rememberLong(exchange, TaskWorkflowExchangeProperties.PROCESS_ID, request.get("id"));

        String correlationId = textValue(request.get("correlationId"));
        if (correlationId == null) {
            correlationId = correlationId(exchange);
            if (correlationId != null) {
                request.put("correlationId", correlationId);
            }
        }
        if (correlationId != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.CORRELATION_ID, correlationId);
        }
        return request;
    }

    private ObjectNode toCompleteProcessRequest(
            Exchange exchange,
            RoutingExecutionContext context
    ) {
        Long processId = context == null ? null : context.processId();
        if (processId == null) {
            processId = exchange.getProperty(
                TaskWorkflowExchangeProperties.PROCESS_ID,
                Long.class
            );
        }
        if (processId == null) {
            processId = pathLong(exchange, "processId");
        }
        if (processId == null) {
            throw new IllegalStateException(
                    "TASK_WORKFLOW cannot complete process: processId is unavailable");
        }

        ObjectNode request = objectMapper.createObjectNode();
        request.put("id", processId);
        request.put("status", "COMPLETE");
        ObjectNode attribute = request.putObject("attribute");
        attribute.put("businessResult", "SUCCESS");
        return request;
    }

    private ObjectNode withTaskId(Exchange exchange, ObjectNode request) {
        Long taskId = pathLong(exchange, "taskId");
        if (taskId != null) {
            request.put("taskId", taskId);
        }
        rememberLong(exchange, TaskWorkflowExchangeProperties.TASK_ID,
                request.get("taskId"));
        return request;
    }

    private ObjectNode withProcessId(Exchange exchange, ObjectNode request) {
        Long processId = pathLong(exchange, "processId");
        if (processId != null) {
            request.put("id", processId);
        }
        rememberLong(exchange, TaskWorkflowExchangeProperties.PROCESS_ID,
                request.get("id"));
        return request;
    }

    private ObjectNode findTasksByProcessId(Exchange exchange, ObjectNode request) {
        Long processId = pathLong(exchange, "processId");
        if (processId == null) {
            throw new IllegalStateException(
                    "TASK_WORKFLOW FIND_TASKS_BY_PROCESS_ID requires processId");
        }
        exchange.getMessage().setHeader("processID", processId);
        exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);
        return request;
    }

    private ObjectNode objectRequest(Exchange exchange, TaskWorkflowRole role) {
        JsonNode body = toJsonNode(inboundBody(exchange));
        if (body.isNull() || body.isMissingNode()) {
            return objectMapper.createObjectNode();
        }
        if (!body.isObject()) {
            throw new IllegalStateException("TASK_WORKFLOW role=" + role
                    + " requires a JSON object payload");
        }
        return ((ObjectNode) body).deepCopy();
    }

    private Object inboundBody(Exchange exchange) {
        Object originalBody = exchange.getProperty(Message.ORIGINAL_BODY);
        Object body = originalBody != null ? originalBody : exchange.getMessage().getBody();
        if (body instanceof Message message) {
            return message.getPayload();
        }
        return body;
    }

    private JsonNode toJsonNode(Object value) {
        if (value instanceof Message message) {
            return message.getPayload() == null
                    ? objectMapper.nullNode()
                    : message.getPayload().deepCopy();
        }
        if (value instanceof JsonNode jsonNode) {
            return jsonNode.deepCopy();
        }
        return value == null ? objectMapper.nullNode() : objectMapper.valueToTree(value);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pathVariables(Exchange exchange) {
        Map<String, Object> variables = exchange.getProperty(
                Message.INBOUND_PATH_VARIABLES,
                Map.class
        );
        return variables == null ? Map.of() : variables;
    }

    private Long pathLong(Exchange exchange, String variableName) {
        Object value = pathVariables(exchange).entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(variableName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("TASK_WORKFLOW path variable "
                    + variableName + " must be a number", exception);
        }
    }

    private void rememberLong(Exchange exchange, String propertyName, JsonNode value) {
        Long parsed = longValue(value);
        if (parsed != null) {
            exchange.setProperty(propertyName, parsed);
        }
    }

    private Long longValue(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        if (value.isIntegralNumber()) {
            return value.longValue();
        }
        try {
            return Long.valueOf(value.asText());
        } catch (NumberFormatException exception) {
            throw new IllegalStateException("TASK_WORKFLOW identifier must be a number", exception);
        }
    }

    private String textValue(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }

    private String correlationId(Exchange exchange) {
        String correlationId = exchange.getProperty(Message.CORRELATION_ID, String.class);
        if (correlationId != null && !correlationId.isBlank()) {
            return correlationId;
        }
        return exchange.getMessage().getHeader(
                SCM_PARAMETER_CLIENT_CORRELATION_ID,
                String.class
        );
    }
}
