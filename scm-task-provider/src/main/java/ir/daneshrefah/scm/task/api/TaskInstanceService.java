package ir.daneshrefah.scm.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.event.TaskProviderEventPublisher;
import ir.daneshrefah.scm.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import ir.daneshrefah.scm.task.model.TaskResponse;
import ir.daneshrefah.scm.task.service.TaskManagementService;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.apache.camel.Header;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;
import static ir.daneshrefah.scm.common.event.provider.ScmProviderEventType.*;

@Service
public class TaskInstanceService extends AbstractJavaService {

    private final TaskManagementService taskManagementService;
    private final TaskProviderEventPublisher eventPublisher;

    public TaskInstanceService(
            ServiceProducerTemplate producerTemplate,
            ObjectMapper objectMapper,
            TaskManagementService taskManagementService,
            TaskProviderEventPublisher eventPublisher
    ) {
        super(producerTemplate, objectMapper);
        this.taskManagementService = taskManagementService;
        this.eventPublisher = eventPublisher;
    }

    @JavaService(operationCode = SVC_CARTABLE_GET_ALL_TASK)
    @SuppressWarnings("unused")
    public PagedResponseData<TaskResponse> findAllTask(Exchange exchange, @Body TaskFilterRequest request) {
        return taskManagementService.findAllTaskByUserIDAndFilter(exchange,request);
    }

    @JavaService(operationCode = SVC_CARTABLE_COMPLTE_TASK)
    @SuppressWarnings("unused")
    public TaskResponse completeTask(Exchange exchange, @Body TaskRequest taskRequest) {
        eventPublisher.publishTask(TASK_COMPLETE_REQUESTED, exchange, taskRequest.getTaskId(),
                null, taskRequest.getAction(), SVC_CARTABLE_COMPLTE_TASK.name(), null);
        try {
            TaskResponse response = taskManagementService.completeTask(exchange, taskRequest);
            Long processId = response.getProcessInstance() == null ? null : response.getProcessInstance().getId();
            eventPublisher.publishTask(TASK_COMPLETED, exchange, response.getId(),
                    processId, response.getTaskStatus(), SVC_CARTABLE_COMPLTE_TASK.name(), null);
            return response;
        } catch (RuntimeException exception) {
            eventPublisher.publishTask(TASK_COMPLETE_FAILED, exchange, taskRequest.getTaskId(),
                    null, taskRequest.getAction(), SVC_CARTABLE_COMPLTE_TASK.name(), exception);
            throw exception;
        }
    }


    @JavaService(operationCode = SVC_CARTABLE_GET_TASK_BY_PROCESS_ID)
    @SuppressWarnings("unused")
    public List<TaskResponse> findAllTasksByProcessId(Exchange exchange, @Header("processID") Long processID) {
       return taskManagementService.findAllTasksByProcessId(exchange,processID);
    }
}
