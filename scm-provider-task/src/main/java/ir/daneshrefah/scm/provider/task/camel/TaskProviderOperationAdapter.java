package ir.daneshrefah.scm.provider.task.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.provider.task.api.ProcessInstanceService;
import ir.daneshrefah.scm.provider.task.api.TaskInstanceService;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceApproveRequest;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceCancelRequest;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceCompleteRequest;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceFilterRequest;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceStartRequest;
import ir.daneshrefah.scm.provider.task.model.ProcessInstanceUpdateRequest;
import ir.daneshrefah.scm.provider.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.provider.task.model.TaskRequest;
import org.apache.camel.Exchange;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class TaskProviderOperationAdapter {
    private static final Set<OperationCode> SUPPORTED_OPERATIONS = Set.of(
            OperationCode.SVC_CARTABLE_START_PROCESS,
            OperationCode.SVC_CARTABLE_APPROVE_PROCESS,
            OperationCode.SVC_CARTABLE_COMPLETE_PROCESS,
            OperationCode.SVC_CARTABLE_CANCEL_PROCESS,
            OperationCode.SVC_CARTABLE_COMPLTE_TASK,
            OperationCode.SVC_CARTABLE_GET_ALL_TASK,
            OperationCode.SVC_CARTABLE_GET_ALL_PROCESS,
            OperationCode.SVC_CARTABLE_GET_TASK_BY_PROCESS_ID,
            OperationCode.SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION
    );

    private final ObjectMapper objectMapper;
    private final ProcessInstanceService processInstanceService;
    private final TaskInstanceService taskInstanceService;

    public TaskProviderOperationAdapter(
            ObjectMapper objectMapper,
            ProcessInstanceService processInstanceService,
            TaskInstanceService taskInstanceService
    ) {
        this.objectMapper = objectMapper;
        this.processInstanceService = processInstanceService;
        this.taskInstanceService = taskInstanceService;
    }

    public boolean supports(String operationCode) {
        try {
            return SUPPORTED_OPERATIONS.contains(resolveOperation(operationCode));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public void execute(String operationCode, Exchange exchange) {
        OperationCode operation = resolveOperation(operationCode);
        if (!SUPPORTED_OPERATIONS.contains(operation)) {
            throw new IllegalArgumentException(
                    "Unsupported " + TaskProviderComponent.SCHEME
                            + " operationCode=" + operationCode);
        }

        Object response = switch (operation) {
            case SVC_CARTABLE_START_PROCESS -> processInstanceService.start(
                    exchange,
                    request(exchange, ProcessInstanceStartRequest.class)
            );
            case SVC_CARTABLE_APPROVE_PROCESS -> processInstanceService.approve(
                    exchange,
                    request(exchange, ProcessInstanceApproveRequest.class)
            );
            case SVC_CARTABLE_COMPLETE_PROCESS -> completeProcess(exchange, operation);
            case SVC_CARTABLE_CANCEL_PROCESS -> cancelProcess(exchange, operation);
            case SVC_CARTABLE_COMPLTE_TASK -> taskInstanceService.completeTask(
                    exchange,
                    request(exchange, TaskRequest.class)
            );
            case SVC_CARTABLE_GET_ALL_TASK -> taskInstanceService.findAllTask(
                    exchange,
                    request(exchange, TaskFilterRequest.class)
            );
            case SVC_CARTABLE_GET_ALL_PROCESS -> processInstanceService.findAll(
                    exchange,
                    request(exchange, ProcessInstanceFilterRequest.class)
            );
            case SVC_CARTABLE_GET_TASK_BY_PROCESS_ID ->
                    taskInstanceService.findAllTasksByProcessId(exchange, processId(exchange));
            case SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION ->
                    processInstanceService.updateDescription(
                            exchange,
                            request(exchange, ProcessInstanceUpdateRequest.class)
                    );
            default -> throw new IllegalArgumentException(
                    "Unsupported " + TaskProviderComponent.SCHEME
                            + " operationCode=" + operationCode);
        };
        exchange.getMessage().setBody(response);
    }

    private Object completeProcess(Exchange exchange, OperationCode operation) {
        ProcessInstanceCompleteRequest request = request(
                exchange,
                ProcessInstanceCompleteRequest.class
        );
        processInstanceService.complete(exchange, request);
        return acknowledgement(operation, request.getId(), request.getStatus());
    }

    private Object cancelProcess(Exchange exchange, OperationCode operation) {
        ProcessInstanceCancelRequest request = request(
                exchange,
                ProcessInstanceCancelRequest.class
        );
        processInstanceService.cancelProcess(exchange, request);
        return acknowledgement(operation, request.getId(), "CANCEL");
    }

    private Map<String, Object> acknowledgement(
            OperationCode operation,
            Long processId,
            Object status
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("operationName", operation.name());
        response.put("successful", true);
        if (processId != null) {
            response.put("processId", processId);
        }
        if (status != null) {
            response.put("status", status);
        }
        return Map.copyOf(response);
    }

    private Long processId(Exchange exchange) {
        Long processId = exchange.getMessage().getHeader("processID", Long.class);
        if (processId == null) {
            processId = exchange.getMessage().getHeader("processId", Long.class);
        }
        if (processId == null) {
            JsonNode body = objectMapper.valueToTree(payload(exchange));
            JsonNode value = body.path("processId");
            if (value.isMissingNode() || value.isNull()) {
                value = body.path("id");
            }
            if (!value.isMissingNode() && !value.isNull()) {
                processId = value.isIntegralNumber()
                        ? value.longValue()
                        : Long.valueOf(value.asText());
            }
        }
        if (processId == null) {
            throw new IllegalArgumentException(
                    "SVC_CARTABLE_GET_TASK_BY_PROCESS_ID requires processID header");
        }
        return processId;
    }

    private <T> T request(Exchange exchange, Class<T> requestType) {
        Object payload = payload(exchange);
        if (requestType.isInstance(payload)) {
            return requestType.cast(payload);
        }
        Object source = payload == null ? Map.of() : payload;
        try {
            return objectMapper.convertValue(source, requestType);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid task provider payload for requestType="
                    + requestType.getSimpleName(), exception);
        }
    }

    private Object payload(Exchange exchange) {
        Object body = exchange.getMessage().getBody();
        if (body instanceof Message message) {
            return message.getPayload();
        }
        return body;
    }

    private OperationCode resolveOperation(String operationCode) {
        if (operationCode == null || operationCode.isBlank()) {
            throw new IllegalArgumentException(
                    TaskProviderComponent.SCHEME + " operationCode is required");
        }
        try {
            return OperationCode.valueOf(operationCode.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Unknown " + TaskProviderComponent.SCHEME
                            + " operationCode=" + operationCode,
                    exception
            );
        }
    }
}
