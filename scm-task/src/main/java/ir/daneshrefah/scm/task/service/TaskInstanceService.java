package ir.daneshrefah.scm.task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.model.TaskFilterRequest;
import ir.daneshrefah.scm.task.model.TaskRequest;
import ir.daneshrefah.scm.task.model.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;
import static ir.daneshrefah.scm.common.constant.ServiceCode.SVC_CARTABLE_GET_ALL_TASK;

@Service
public class TaskInstanceService extends AbstractJavaService {

    @Autowired
    private TaskManagementService taskManagementService;

    public TaskInstanceService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService(serviceCode = SVC_CARTABLE_GET_ALL_TASK)
    @SuppressWarnings("unused")
    public PagedResponseData<TaskResponse> findAllTask(TaskFilterRequest request) {
        return taskManagementService.findAllTaskByUserIDAndFilter(request);
    }

    @JavaService(serviceCode = SVC_CARTABLE_COMPLTE_TASK)
    @SuppressWarnings("unused")
    public TaskResponse completeTask(TaskRequest taskRequest) {
        return taskManagementService.completeTask(taskRequest);
    }


    @JavaService(serviceCode = SVC_CARTABLE_GET_TASK_BY_PROCESS_ID)
    @SuppressWarnings("unused")
    public List<TaskResponse> findAllTasksByProcessId(Long processID) {
       return taskManagementService.findAllTasksByProcessId(processID);
    }
}
