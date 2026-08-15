package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowProviderRequestContext;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowProviderRequestFactory;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;

/**
 * Adapts normalized SCM input and core routing state. Task-provider-specific
 * request shapes are delegated to the optional provider-owned factory.
 */
public class TaskWorkflowPayloadMapper {
    private final ObjectMapper objectMapper;
    private final TaskWorkflowInputResolver inputResolver;
    private final ObjectProvider<TaskWorkflowProviderRequestFactory>
            providerRequestFactories;

    public TaskWorkflowPayloadMapper(
            ObjectMapper objectMapper,
            TaskWorkflowInputResolver inputResolver,
            ObjectProvider<TaskWorkflowProviderRequestFactory>
                    providerRequestFactories
    ) {
        this.objectMapper = objectMapper;
        this.inputResolver = inputResolver;
        this.providerRequestFactories = providerRequestFactories;
    }

    public Object toRequest(
            Exchange exchange,
            String stepId,
            TaskWorkflowStepType stepType,
            RoutingExecutionContext context
    ) {
        if (stepId.equals(context.retryStepId())
                && context.retryRequest() != null) {
            return toJsonNode(context.retryRequest());
        }
        if (stepType == TaskWorkflowStepType.BUSINESS_OPERATION) {
            return toBusinessRequest(exchange, context);
        }
        return providerFactory(stepType).create(
                stepType,
                providerContext(exchange, stepType, context)
        );
    }

    public void requireProviderRequestFactory(
            String serviceCode,
            TaskWorkflowStepType stepType
    ) {
        List<TaskWorkflowProviderRequestFactory> matching =
                matchingFactories(stepType);
        if (matching.size() != 1) {
            throw new IllegalStateException(
                    "Active TASK_WORKFLOW serviceCode=" + serviceCode
                            + " requires exactly one "
                            + "TaskWorkflowProviderRequestFactory for "
                            + "stepType=" + stepType + "; found "
                            + matching.size()
            );
        }
    }

    private TaskWorkflowProviderRequestContext providerContext(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            RoutingExecutionContext context
    ) {
        Long processId = switch (stepType) {
            case APPROVE_PROCESS, COMPLETE_PROCESS, REJECT_PROCESS ,
                 GET_TASK , UPDATE_DESCRIPTION ->
                    requireProcessId(exchange, stepType, context);
            default -> resolveProcessId(exchange, stepType, context);
        };
        Long taskId = stepType == TaskWorkflowStepType.TASK_COMPLETE
                ? inputResolver.requireTaskId(exchange, stepType)
                : null;
        if (processId != null) {
            context.processId(processId);
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    processId
            );
        }
        if (taskId != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.TASK_ID,
                    taskId
            );
        }
        String processCorrelationId = context.correlationId();
        if (processCorrelationId == null) {
            processCorrelationId = exchange.getProperty(
                    TaskWorkflowExchangeProperties.CORRELATION_ID,
                    String.class
            );
            context.correlationId(processCorrelationId);
        }
        return new TaskWorkflowProviderRequestContext(
                inboundPayload(exchange),
                processId,
                taskId,
                processCorrelationId
        );
    }

    private ObjectNode toBusinessRequest(
            Exchange exchange,
            RoutingExecutionContext context
    ) {
        Long processId = resolveProcessId(
                exchange,
                TaskWorkflowStepType.BUSINESS_OPERATION,
                context
        );
        context.processId(processId);
        if (processId != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    processId
            );
        }
        String correlationId = context.correlationId();
        if (correlationId != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.CORRELATION_ID,
                    correlationId
            );
        }
        Object stableTransactionData = null;
        try {
            Object body = exchange.getMessage().getBody();
            stableTransactionData = toJsonNode(body).get("transactionData");
        } catch (Exception e) {
        }
        if (stableTransactionData == null) {
            stableTransactionData = context.transactionData();
        }
        context.transactionData(stableTransactionData);
        ObjectNode request = objectMapper.createObjectNode();
        if (processId != null) {
            request.put("processId", processId);
        }
        if (correlationId != null) {
            request.put("correlationId", correlationId);
        }
        request.set("transactionData", toJsonNode(stableTransactionData));
        request.set(
                "stepResults",
                objectMapper.valueToTree(context.stepResults())
        );

        if (stableTransactionData == null) {
            JsonNode node = inboundPayload(exchange);
            if (node != null){
                if (node instanceof JsonNode && (!node.asText().isEmpty() || node.size() > 0)) {
                    request = (ObjectNode) node;
                }
            }
//            stableTransactionData = objectMapper.createObjectNode();
        }

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
        String correlationId = textValue(
                approveResponse.get("correlationId")
        );
        if (correlationId != null) {
            context.correlationId(correlationId);
        }
        JsonNode transactionData = approveResponse.get("transactionData");
        if (transactionData != null && !transactionData.isNull()) {
            context.transactionData(transactionData.deepCopy());
        }
    }

    public void rememberStepContext(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            Object response,
            RoutingExecutionContext context
    ) {
        if (stepType == TaskWorkflowStepType.APPROVE_PROCESS) {
            rememberApproveContext(response, context);
        } else if (context.processId() == null) {
            JsonNode responseNode = toJsonNode(response);
            Long processId = firstLong(
                    responseNode.path("processId"),
                    responseNode.path("id"),
                    responseNode.path("processInstance").path("id")
            );
            if (processId != null) {
                context.processId(processId);
            }
        }
        if (context.processId() != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    context.processId()
            );
        }
    }

    private TaskWorkflowProviderRequestFactory providerFactory(
            TaskWorkflowStepType stepType
    ) {
        List<TaskWorkflowProviderRequestFactory> matching =
                matchingFactories(stepType);
        if (matching.size() != 1) {
            throw new IllegalStateException(
                    "Expected exactly one TaskWorkflowProviderRequestFactory "
                            + "for stepType=" + stepType + "; found "
                            + matching.size()
            );
        }
        return matching.getFirst();
    }

    private List<TaskWorkflowProviderRequestFactory> matchingFactories(
            TaskWorkflowStepType stepType
    ) {
        return providerRequestFactories.orderedStream()
                .filter(factory -> factory.supports(stepType))
                .toList();
    }

    private Long requireProcessId(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            RoutingExecutionContext context
    ) {
        Long processId = resolveProcessId(exchange, stepType, context);
        if (processId == null) {
            throw new IllegalStateException(
                    "TASK_WORKFLOW stepType=" + stepType
                            + " requires processId"
            );
        }
        return processId;
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
        if (contextProcessId != null
                && storedProcessId != null
                && !contextProcessId.equals(storedProcessId)) {
            throw new IllegalStateException(
                    "Conflicting TASK_WORKFLOW processId values were "
                            + "supplied by workflow execution context and "
                            + "prior workflow state"
            );
        }
        Long workflowProcessId = contextProcessId != null
                ? contextProcessId
                : storedProcessId;
        return workflowProcessId != null
                ? workflowProcessId
                : inputResolver.resolveProcessId(exchange, stepType);
    }

    private JsonNode inboundPayload(Exchange exchange) {
        Message normalizedMessage = exchange.getProperty(
                Message.INTERNAL_MESSAGE,
                Message.class
        );
        if (normalizedMessage != null) {
            return toJsonNode(normalizedMessage.getPayload());
        }
        Object originalBody = exchange.getProperty(Message.ORIGINAL_BODY);
        Object body = originalBody != null
                ? originalBody
                : exchange.getMessage().getBody();
        return toJsonNode(body);
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
        return value == null
                ? objectMapper.nullNode()
                : objectMapper.valueToTree(value);
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
            throw new IllegalStateException(
                    "TASK_WORKFLOW identifier must be a number",
                    exception
            );
        }
    }

    private Long firstLong(JsonNode... values) {
        for (JsonNode value : values) {
            Long resolved = longValue(value);
            if (resolved != null) {
                return resolved;
            }
        }
        return null;
    }

    private String textValue(JsonNode value) {
        if (value == null || value.isNull() || value.isMissingNode()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }
}
