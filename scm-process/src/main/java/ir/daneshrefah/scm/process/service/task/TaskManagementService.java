package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.task.TaskCompleteRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskFindRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class TaskManagementService extends AbstractJavaService {

    private final TaskService taskService;

    public TaskManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, TaskService taskService) {
        super(producerTemplate, objectMapper);
        this.taskService = taskService;
    }

//    @PreAuthorize("isFullyAuthenticated()")
    @JavaService
    public PagedResponseData findTaskList(TaskFindRequest taskFindRequest) throws JsonProcessingException {
        if (Objects.isNull(taskFindRequest)) {
            taskFindRequest = new TaskFindRequest();
        }
//        UserAuthentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
//        if (StringUtils.isBlank(taskFindRequest.getAssignee()) || !authentication.hasAuthority(ROLE_ADMIN_BPM)) {
//            String assignee = authentication.getPrincipal().getPerson().getUsername();
//            taskFindRequest.setAssignee(assignee);
//        }
        return taskService.findTaskList(taskFindRequest);
    }

    @JavaService
    public boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException {
        return taskService.completeTask(taskRequest);
    }
}
