package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class InternalTaskWorkflowEngine implements TaskWorkflowEngine {
    public static final String ENGINE_TYPE = "internal";

    private static final Set<TaskWorkflowRole> SUPPORTED_ROLES = EnumSet.of(
            TaskWorkflowRole.START_PROCESS,
            TaskWorkflowRole.APPROVE_PROCESS,
            TaskWorkflowRole.COMPLETE_PROCESS,
            TaskWorkflowRole.CANCEL_PROCESS,
            TaskWorkflowRole.COMPLETE_TASK,
            TaskWorkflowRole.FIND_ALL_TASK,
            TaskWorkflowRole.FIND_ALL_PROCESS,
            TaskWorkflowRole.FIND_TASK_BY_PROCESS_ID,
            TaskWorkflowRole.UPDATE_PROCESS_DESCRIPTION
    );

    private final ObjectMapper objectMapper;
    private final ProcessInstanceService processInstanceService;
    private final TaskInstanceService taskInstanceService;

    public InternalTaskWorkflowEngine(
            ObjectMapper objectMapper,
            ProcessInstanceService processInstanceService,
            TaskInstanceService taskInstanceService
    ) {
        this.objectMapper = objectMapper;
        this.processInstanceService = processInstanceService;
        this.taskInstanceService = taskInstanceService;
    }

    @Override
    public String engineType() {
        return ENGINE_TYPE;
    }

    @Override
    public boolean supports(TaskWorkflowRole role) {
        return SUPPORTED_ROLES.contains(role);
    }

    @Override
    public Object execute(TaskWorkflowRole role, Exchange exchange) {
        return switch (role) {
            case START_PROCESS -> processInstanceService.start(
                    exchange,
                    request(exchange, ProcessInstanceStartRequest.class)
            );
            case APPROVE_PROCESS -> processInstanceService.approve(
                    exchange,
                    request(exchange, ProcessInstanceApproveRequest.class)
            );
            case COMPLETE_PROCESS -> completeProcess(exchange, role);
            case CANCEL_PROCESS -> cancelProcess(exchange, role);
            case COMPLETE_TASK -> taskInstanceService.completeTask(
                    exchange,
                    request(exchange, TaskRequest.class)
            );
            case FIND_ALL_TASK -> taskInstanceService.findAllTask(
                    exchange,
                    request(exchange, TaskFilterRequest.class)
            );
            case FIND_ALL_PROCESS -> processInstanceService.findAll(
                    exchange,
                    request(exchange, ProcessInstanceFilterRequest.class)
            );
            case FIND_TASK_BY_PROCESS_ID ->
                    taskInstanceService.findAllTasksByProcessId(exchange, processId(exchange));
            case UPDATE_PROCESS_DESCRIPTION ->
                    processInstanceService.updateDescription(
                            exchange,
                            request(exchange, ProcessInstanceUpdateRequest.class)
                    );
        };
    }

    private Object completeProcess(Exchange exchange, TaskWorkflowRole role) {
        ProcessInstanceCompleteRequest request = request(
                exchange,
                ProcessInstanceCompleteRequest.class
        );
        processInstanceService.complete(exchange, request);
        return acknowledgement(role, request.getId(), request.getStatus());
    }

    private Object cancelProcess(Exchange exchange, TaskWorkflowRole role) {
        ProcessInstanceCancelRequest request = request(
                exchange,
                ProcessInstanceCancelRequest.class
        );
        processInstanceService.cancelProcess(exchange, request);
        return acknowledgement(role, request.getId(), "CANCEL");
    }

    private Map<String, Object> acknowledgement(
            TaskWorkflowRole role,
            Long processId,
            Object status
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("taskWorkflowRole", role.name());
        response.put("operationName", role.name());
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
                    "FIND_TASK_BY_PROCESS_ID requires processID header");
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
}
