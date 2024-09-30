package ir.daneshrefah.scm.task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import ir.daneshrefah.scm.task.model.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskInstanceService extends AbstractJavaService {

    @Autowired
    private TaskManagementService taskManagementService;

    public TaskInstanceService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService
    @SuppressWarnings("unused")
    public PagedResponseData<TaskResponse> findAllTask(TaskFilterRequest request) {
        return taskManagementService.findAllTaskByUserIDAndFilter(request);
    }

    @JavaService
    @SuppressWarnings("unused")
    public TaskResponse completeTask(TaskRequest taskRequest) {
        return taskManagementService.completeTask(taskRequest);
    }
    @JavaService
    @SuppressWarnings("unused")
    public List<TaskResponse> findAllTasksByProcessId(Long processID,String loggedInUserID) {//TODO remove loggedInUserID and from service table
       return taskManagementService.findAllTasksByProcessId(processID,loggedInUserID);
    }
}
