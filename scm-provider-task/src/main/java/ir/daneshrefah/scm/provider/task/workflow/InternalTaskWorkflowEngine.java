package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
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

    private static final Set<TaskWorkflowStepType> SUPPORTED_STEP_TYPES = EnumSet.of(
            TaskWorkflowStepType.START_PROCESS,
            TaskWorkflowStepType.APPROVE_PROCESS,
            TaskWorkflowStepType.COMPLETE_PROCESS,
            TaskWorkflowStepType.REJECT_PROCESS,
            TaskWorkflowStepType.TASK_COMPLETE,
            TaskWorkflowStepType.GET_ALL_TASK,
            TaskWorkflowStepType.GET_ALL_PROCESS,
            TaskWorkflowStepType.GET_TASK,
            TaskWorkflowStepType.UPDATE_DESCRIPTION
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
    public boolean supports(TaskWorkflowStepType stepType) {
        return SUPPORTED_STEP_TYPES.contains(stepType);
    }

    @Override
    public boolean isExplicitSuccess(
            TaskWorkflowStepType stepType,
            Object result
    ) {
        return supports(stepType) && result != null;
    }

    @Override
    public Object execute(TaskWorkflowStepType stepType, Exchange exchange) {
        if (!supports(stepType)) {
            throw unsupported(stepType);
        }
        return switch (stepType) {
            case START_PROCESS -> processInstanceService.start(
                    exchange,
                    request(exchange, ProcessInstanceStartRequest.class)
            );
            case APPROVE_PROCESS -> processInstanceService.approve(
                    exchange,
                    request(exchange, ProcessInstanceApproveRequest.class)
            );
            case COMPLETE_PROCESS -> completeProcess(exchange, stepType);
            case REJECT_PROCESS -> cancelProcess(exchange, stepType);
            case TASK_COMPLETE -> taskInstanceService.completeTask(
                    exchange,
                    request(exchange, TaskRequest.class)
            );
            case GET_ALL_TASK -> taskInstanceService.findAllTask(
                    exchange,
                    request(exchange, TaskFilterRequest.class)
            );
            case GET_ALL_PROCESS -> processInstanceService.findAll(
                    exchange,
                    request(exchange, ProcessInstanceFilterRequest.class)
            );
            case GET_TASK ->
                    taskInstanceService.findAllTasksByProcessId(exchange, processId(exchange));
            case UPDATE_DESCRIPTION ->
                    processInstanceService.updateDescription(
                            exchange,
                            request(exchange, ProcessInstanceUpdateRequest.class)
                    );
            case BUSINESS_OPERATION
//                 FIND_PROCUREMENT_BY_ACCOUNT,
//                 FIND_PROCUREMENT_BY_NATIONAL,
//                 PROCUREMENT_STATEMENT_INQUIRY
                    -> throw unsupported(stepType);
        };
    }

    private Object completeProcess(
            Exchange exchange,
            TaskWorkflowStepType stepType
    ) {
        ProcessInstanceCompleteRequest request = request(
                exchange,
                ProcessInstanceCompleteRequest.class
        );
        processInstanceService.complete(exchange, request);
        return acknowledgement(exchange, stepType, request.getId(), request.getStatus());
    }

    private Object cancelProcess(
            Exchange exchange,
            TaskWorkflowStepType stepType
    ) {
        ProcessInstanceCancelRequest request = request(
                exchange,
                ProcessInstanceCancelRequest.class
        );
        processInstanceService.cancelProcess(exchange, request);
        return acknowledgement(exchange, stepType, request.getId(), "CANCEL");
    }

    private Map<String, Object> acknowledgement(
            Exchange exchange,
            TaskWorkflowStepType stepType,
            Long processId,
            Object status
    ) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("taskWorkflowStepType", stepType.name());
        String operationName = exchange.getProperty(Message.OPERATION_NAME, String.class);
        if (operationName != null) {
            response.put("operationName", operationName);
        }
        response.put("successful", true);
        if (processId != null) {
            response.put("processId", processId);
        }
        if (status != null) {
            response.put("status", status);
        }
        if (status != "CANCEL") {
            if (exchange != null) {
                if (exchange.getProperty("transactionData") != null) {
                    response.put("transactionData", exchange.getProperty("transactionData", JsonNode.class));
                }
                if (exchange.getProperty("users") != null) {
                    response.put("users", exchange.getProperty("users", JsonNode.class));
                }
            }
        }
        return Map.copyOf(response);
    }

    private Long processId(Exchange exchange) {
        Long processId = null;
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
        if (processId == null) {
            throw new IllegalArgumentException(
                    "FIND_TASK_BY_PROCESS_ID requires processId in normalized input");
        }
        return processId;
    }

    private IllegalArgumentException unsupported(TaskWorkflowStepType stepType) {
        return new IllegalArgumentException("Task workflow engine-type=" + ENGINE_TYPE
                + " does not support stepType=" + stepType);
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
