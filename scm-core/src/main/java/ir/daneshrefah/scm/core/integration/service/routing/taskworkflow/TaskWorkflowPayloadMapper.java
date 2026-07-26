package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

@Component
public class TaskWorkflowPayloadMapper {
    private final ObjectMapper objectMapper;
    private final TaskWorkflowInputResolver inputResolver;

    public TaskWorkflowPayloadMapper(
            ObjectMapper objectMapper,
            TaskWorkflowInputResolver inputResolver
    ) {
        this.objectMapper = objectMapper;
        this.inputResolver = inputResolver;
    }

    public Object toRequest(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            RoutingExecutionContext context
    ) {
        return switch (stepType) {
            case APPROVE_PROCESS -> toApproveRequest(exchange);
            case BUSINESS_OPERATION -> toBusinessRequest(exchange, context);
            case COMPLETE_PROCESS -> toCompleteProcessRequest(exchange, context);
            default -> toSimpleRequest(exchange, stepType);
        };
    }

    private ObjectNode toBusinessRequest(Exchange exchange, RoutingExecutionContext context) {
        Long processId = resolveProcessId(
                exchange,
                TaskWorkflowStepType.BUSINESS_OPERATION,
                context
        );
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

    public JsonNode toSimpleRequest(Exchange exchange, TaskWorkflowStepType stepType) {
        ObjectNode request = objectRequest(exchange, stepType);
        return switch (stepType) {
            case START_PROCESS, FIND_ALL_PROCESS, FIND_ALL_TASK -> request;
            case COMPLETE_TASK -> withTaskId(exchange, request, stepType);
            case CANCEL_PROCESS, UPDATE_PROCESS_DESCRIPTION ->
                    withProcessId(exchange, request, stepType);
            case FIND_TASK_BY_PROCESS_ID -> findTasksByProcessId(exchange, request, stepType);
            case APPROVE_PROCESS, BUSINESS_OPERATION, COMPLETE_PROCESS ->
                    throw new IllegalStateException("stepType " + stepType
                    + " requires the coordinated APPROVE_AND_EXECUTE flow");
        };
    }

    public ObjectNode toApproveRequest(Exchange exchange) {
        TaskWorkflowStepType stepType = TaskWorkflowStepType.APPROVE_PROCESS;
        ObjectNode request = objectRequest(exchange, stepType);
        Long processId = inputResolver.requireProcessId(exchange, stepType);
        request.put("id", processId);
        exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);

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
        TaskWorkflowStepType stepType = TaskWorkflowStepType.COMPLETE_PROCESS;
        Long processId = resolveProcessId(exchange, stepType, context);
        if (processId == null) {
            throw new IllegalStateException("TASK_WORKFLOW stepType=" + stepType
                    + " requires processId");
        }

        ObjectNode request = objectMapper.createObjectNode();
        request.put("id", processId);
        request.put("status", "COMPLETE");
        ObjectNode attribute = request.putObject("attribute");
        attribute.put("businessResult", "SUCCESS");
        return request;
    }

    private ObjectNode withTaskId(
            Exchange exchange,
            ObjectNode request,
            TaskWorkflowStepType stepType
    ) {
        Long taskId = inputResolver.requireTaskId(exchange, stepType);
        request.put("taskId", taskId);
        exchange.setProperty(TaskWorkflowExchangeProperties.TASK_ID, taskId);
        return request;
    }

    private ObjectNode withProcessId(
            Exchange exchange,
            ObjectNode request,
            TaskWorkflowStepType stepType
    ) {
        Long processId = inputResolver.requireProcessId(exchange, stepType);
        request.put("id", processId);
        exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);
        return request;
    }

    private ObjectNode findTasksByProcessId(
            Exchange exchange,
            ObjectNode request,
            TaskWorkflowStepType stepType
    ) {
        Long processId = inputResolver.requireProcessId(exchange, stepType);
        request.put("processId", processId);
        exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);
        return request;
    }

    private Long resolveProcessId(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            RoutingExecutionContext context
    ) {
        Long contextProcessId = context == null ? null : context.processId();
        Long storedProcessId = exchange.getProperty(
                TaskWorkflowExchangeProperties.PROCESS_ID,
                Long.class
        );
        if (contextProcessId != null && storedProcessId != null
                && !contextProcessId.equals(storedProcessId)) {
            throw new IllegalStateException("Conflicting TASK_WORKFLOW processId values were "
                    + "supplied by workflow execution context and prior workflow state");
        }
        Long workflowProcessId = contextProcessId != null ? contextProcessId : storedProcessId;
        if (workflowProcessId != null) {
            return workflowProcessId;
        }
        return inputResolver.resolveProcessId(exchange, stepType);
    }

    private ObjectNode objectRequest(Exchange exchange, TaskWorkflowStepType stepType) {
        JsonNode body = toJsonNode(inboundBody(exchange));
        if (body.isNull() || body.isMissingNode()) {
            return objectMapper.createObjectNode();
        }
        if (!body.isObject()) {
            throw new IllegalStateException("TASK_WORKFLOW stepType=" + stepType
                    + " requires a JSON object payload");
        }
        return ((ObjectNode) body).deepCopy();
    }

    private Object inboundBody(Exchange exchange) {
        Message normalizedMessage = exchange.getProperty(
                Message.INTERNAL_MESSAGE,
                Message.class
        );
        if (normalizedMessage != null) {
            return normalizedMessage.getPayload();
        }
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
