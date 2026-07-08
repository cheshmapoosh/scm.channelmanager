package ir.daneshrefah.scm.provider.task.camel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.operation.Operation;
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
    public static final String INTERNAL_PROVIDER_CODE = "internal";
    private static final Set<String> SUPPORTED_PROVIDER_CODES = Set.of(
            INTERNAL_PROVIDER_CODE
    );
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

    public void execute(
            String providerCode,
            String legacyOperationCode,
            Exchange exchange
    ) {
        requireSupportedProvider(providerCode);
        String operationCode = resolveOperationCode(exchange, legacyOperationCode);
        OperationCode operation = requireSupportedOperation(operationCode);

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

    String requireSupportedProvider(String providerCode) {
        if (providerCode == null || providerCode.isBlank()) {
            throw new IllegalArgumentException(
                    TaskProviderComponent.SCHEME
                            + " endpoint requires a non-blank providerCode");
        }
        if (!providerCode.equals(providerCode.trim())) {
            throw invalidProviderCode(providerCode,
                    "leading or trailing whitespace is not allowed");
        }
        if (!SUPPORTED_PROVIDER_CODES.contains(providerCode)) {
            throw invalidProviderCode(providerCode,
                    "providerCode is not registered");
        }
        return providerCode;
    }

    boolean supportsOperation(String operationCode) {
        try {
            requireSupportedOperation(operationCode);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    OperationCode requireSupportedOperation(String operationCode) {
        if (operationCode == null || operationCode.isBlank()) {
            throw new IllegalArgumentException(
                    TaskProviderComponent.SCHEME
                            + " provider requires a non-blank Operation.name");
        }
        if (!operationCode.equals(operationCode.trim())) {
            throw invalidOperationName(operationCode,
                    "leading or trailing whitespace is not allowed");
        }
        if (operationCode.regionMatches(true, 0, "op.", 0, "op.".length())) {
            throw invalidOperationName(operationCode,
                    "do not include the generated op. route prefix");
        }
        OperationCode operation;
        try {
            operation = OperationCode.valueOf(operationCode);
        } catch (IllegalArgumentException exception) {
            throw invalidOperationName(operationCode,
                    "name is not an exact OperationCode", exception);
        }
        if (!SUPPORTED_OPERATIONS.contains(operation)) {
            throw invalidOperationName(operationCode,
                    "OperationCode is not exposed by the task provider");
        }
        return operation;
    }

    private String resolveOperationCode(
            Exchange exchange,
            String legacyOperationCode
    ) {
        String operationCode = nonBlank(exchange.getProperty(Message.OPERATION_NAME));
        if (operationCode != null) {
            return operationCode;
        }

        Object operationValue = exchange.getProperty(Message.OPERATION);
        if (operationValue instanceof Operation operationModel) {
            operationCode = nonBlank(operationModel.getName());
            if (operationCode != null) {
                return operationCode;
            }
        } else if (operationValue instanceof OperationCode operationEnum) {
            return operationEnum.name();
        }

        operationCode = nonBlank(legacyOperationCode);
        if (operationCode != null) {
            return operationCode;
        }

        throw new IllegalArgumentException(
                TaskProviderComponent.SCHEME + ":" + INTERNAL_PROVIDER_CODE
                        + " requires operationCode from Exchange property "
                        + Message.OPERATION_NAME + " or " + Message.OPERATION
                        + ".name; legacy URI operationCode is supported only by "
                        + TaskProviderComponent.SCHEME + ":<operationCode>");
    }

    private String nonBlank(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private IllegalArgumentException invalidProviderCode(
            String providerCode,
            String reason
    ) {
        String message = "Invalid " + TaskProviderComponent.SCHEME
                + " providerCode=" + providerCode
                + ": " + reason
                + "; expected one of " + SUPPORTED_PROVIDER_CODES;
        return new IllegalArgumentException(message);
    }

    private IllegalArgumentException invalidOperationName(
            String operationCode,
            String reason
    ) {
        return invalidOperationName(operationCode, reason, null);
    }

    private IllegalArgumentException invalidOperationName(
            String operationCode,
            String reason,
            Throwable cause
    ) {
        String supported = SUPPORTED_OPERATIONS.stream()
                .map(OperationCode::name)
                .sorted()
                .toList()
                .toString();
        String message = "Invalid " + TaskProviderComponent.SCHEME
                + " provider Operation.name=" + operationCode
                + ": " + reason
                + "; expected one of " + supported;
        return cause == null
                ? new IllegalArgumentException(message)
                : new IllegalArgumentException(message, cause);
    }
}
