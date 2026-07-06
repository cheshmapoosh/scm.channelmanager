package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.model.ProcessInstanceApproveRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceApproveResponse;
import ir.daneshrefah.scm.task.model.ProcessInstanceCancelRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceCompleteRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceFilterRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceStartRequest;
import ir.daneshrefah.scm.task.model.ProcessInstanceUpdateRequest;
import ir.daneshrefah.scm.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Supplier;

import static ir.daneshrefah.scm.utils.constant.Constants.SCM_PARAMETER_CLIENT_CORRELATION_ID;

@Component
public class TaskWorkflowPayloadMapper {
    private final ObjectMapper objectMapper;

    public TaskWorkflowPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Object toSimpleRequest(Exchange exchange, TaskWorkflowRole role) {
        Object body = inboundBody(exchange);
        return switch (role) {
            case START_PROCESS -> convert(body, ProcessInstanceStartRequest.class,
                    ProcessInstanceStartRequest::new);
            case COMPLETE_TASK -> completeTaskRequest(exchange, body);
            case CANCEL_PROCESS -> cancelProcessRequest(exchange, body);
            case FIND_PROCESSES -> convert(body, ProcessInstanceFilterRequest.class,
                    ProcessInstanceFilterRequest::new);
            case FIND_TASKS -> convert(body, TaskFilterRequest.class, TaskFilterRequest::new);
            case FIND_TASKS_BY_PROCESS_ID -> findTasksByProcessId(exchange, body);
            case UPDATE_PROCESS_DESCRIPTION -> updateProcessDescriptionRequest(exchange, body);
            case APPROVE_PROCESS, BUSINESS_OPERATION, COMPLETE_PROCESS ->
                    throw new IllegalStateException("Role " + role
                            + " requires the coordinated APPROVE_AND_EXECUTE flow");
        };
    }

    public ProcessInstanceApproveRequest toApproveRequest(Exchange exchange) {
        ProcessInstanceApproveRequest request = convert(
                inboundBody(exchange),
                ProcessInstanceApproveRequest.class,
                ProcessInstanceApproveRequest::new
        );
        Long processId = pathLong(exchange, "processId");
        if (processId != null) {
            request.setId(processId);
        }
        if (request.getId() != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, request.getId());
        }
        if (request.getCorrelationId() == null) {
            request.setCorrelationId(correlationId(exchange));
        }
        if (request.getCorrelationId() != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.CORRELATION_ID,
                    request.getCorrelationId()
            );
        }
        return request;
    }

    public Object toBusinessRequest(Exchange exchange) {
        Object approveValue = exchange.getProperty(
                TaskWorkflowExchangeProperties.APPROVE_RESPONSE);
        ProcessInstanceApproveResponse approveResponse = convert(
                approveValue,
                ProcessInstanceApproveResponse.class,
                ProcessInstanceApproveResponse::new
        );
        if (approveResponse.getId() != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    approveResponse.getId()
            );
        }
        JsonNode transactionData = approveResponse.getTransactionData();
        return transactionData == null || transactionData.isNull()
                ? objectMapper.valueToTree(approveResponse)
                : transactionData;
    }

    public ProcessInstanceCompleteRequest toCompleteProcessRequest(
            Exchange exchange,
            TaskWorkflowBusinessResultClassifier.BusinessResult businessResult
    ) {
        ProcessInstanceCompleteRequest request = new ProcessInstanceCompleteRequest();
        Long processId = exchange.getProperty(
                TaskWorkflowExchangeProperties.PROCESS_ID,
                Long.class
        );
        if (processId == null) {
            processId = pathLong(exchange, "processId");
        }
        if (processId == null) {
            throw new IllegalStateException("TASK_WORKFLOW cannot complete process: processId is unavailable");
        }
        request.setId(processId);
        request.setStatus(businessResult
                == TaskWorkflowBusinessResultClassifier.BusinessResult.SUCCESS
                ? ProcessStatusEnum.COMPLETE
                : ProcessStatusEnum.FAIL);
        ObjectNode attribute = objectMapper.createObjectNode();
        attribute.put("businessResult", businessResult.name());
        request.setAttribute(attribute);
        return request;
    }

    public ProcessInstanceApproveResponse toApproveResponse(Object response) {
        return convert(response, ProcessInstanceApproveResponse.class,
                ProcessInstanceApproveResponse::new);
    }

    private TaskRequest completeTaskRequest(Exchange exchange, Object body) {
        TaskRequest request = convert(body, TaskRequest.class, TaskRequest::new);
        Long taskId = pathLong(exchange, "taskId");
        if (taskId != null) {
            request.setTaskId(taskId);
        }
        if (request.getTaskId() != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.TASK_ID, request.getTaskId());
        }
        return request;
    }

    private ProcessInstanceCancelRequest cancelProcessRequest(Exchange exchange, Object body) {
        ProcessInstanceCancelRequest request = convert(
                body,
                ProcessInstanceCancelRequest.class,
                ProcessInstanceCancelRequest::new
        );
        Long processId = pathLong(exchange, "processId");
        if (processId != null) {
            request.setId(processId);
        }
        if (request.getId() != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, request.getId());
        }
        return request;
    }

    private Object findTasksByProcessId(Exchange exchange, Object body) {
        Long processId = pathLong(exchange, "processId");
        if (processId == null) {
            throw new IllegalStateException("TASK_WORKFLOW FIND_TASKS_BY_PROCESS_ID requires processId");
        }
        exchange.getMessage().setHeader("processID", processId);
        exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, processId);
        return body;
    }

    private ProcessInstanceUpdateRequest updateProcessDescriptionRequest(
            Exchange exchange,
            Object body
    ) {
        ProcessInstanceUpdateRequest request = convert(
                body,
                ProcessInstanceUpdateRequest.class,
                ProcessInstanceUpdateRequest::new
        );
        Long processId = pathLong(exchange, "processId");
        if (processId != null) {
            request.setId(processId);
        }
        if (request.getId() != null) {
            exchange.setProperty(TaskWorkflowExchangeProperties.PROCESS_ID, request.getId());
        }
        return request;
    }

    private Object inboundBody(Exchange exchange) {
        Object originalBody = exchange.getProperty(Message.ORIGINAL_BODY);
        Object body = originalBody != null ? originalBody : exchange.getMessage().getBody();
        if (body instanceof Message message) {
            return message.getPayload();
        }
        return body;
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

    private <T> T convert(Object source, Class<T> type, Supplier<T> emptyFactory) {
        if (source == null) {
            return emptyFactory.get();
        }
        if (type.isInstance(source)) {
            return type.cast(source);
        }
        return objectMapper.convertValue(source, type);
    }
}
