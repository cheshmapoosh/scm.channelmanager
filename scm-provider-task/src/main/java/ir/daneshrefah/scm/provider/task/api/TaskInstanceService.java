package ir.daneshrefah.scm.provider.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.provider.task.event.TaskProviderEventPublisher;
import ir.daneshrefah.scm.provider.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.provider.task.model.TaskRequest;
import ir.daneshrefah.scm.provider.task.model.TaskResponse;
import ir.daneshrefah.scm.provider.task.service.TaskManagementService;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @SuppressWarnings("unused")
    public PagedResponseData<TaskResponse> findAllTask(Exchange exchange, @Body TaskFilterRequest request) {
        return taskManagementService.findAllTaskByUserIDAndFilter(exchange,request);
    }

    @SuppressWarnings("unused")
    public TaskResponse completeTask(Exchange exchange, @Body TaskRequest taskRequest) {
        String operationName = exchange == null
                ? null
                : exchange.getProperty(Message.OPERATION_NAME, String.class);
        eventPublisher.publishTask(TASK_COMPLETE_REQUESTED, exchange, taskRequest.getTaskId(),
                null, taskRequest.getAction(), operationName, null);
        try {
            TaskResponse response = taskManagementService.completeTask(exchange, taskRequest);
            Long processId = response.getProcessInstance() == null ? null : response.getProcessInstance().getId();
            eventPublisher.publishTask(TASK_COMPLETED, exchange, response.getId(),
                    processId, response.getTaskStatus(), operationName, null);
            return response;
        } catch (RuntimeException exception) {
            eventPublisher.publishTask(TASK_COMPLETE_FAILED, exchange, taskRequest.getTaskId(),
                    null, taskRequest.getAction(), operationName, exception);
            throw exception;
        }
    }


    @SuppressWarnings("unused")
    public List<TaskResponse> findAllTasksByProcessId(Exchange exchange, Long processId) {
        return taskManagementService.findAllTasksByProcessId(exchange, processId);
    }
}
