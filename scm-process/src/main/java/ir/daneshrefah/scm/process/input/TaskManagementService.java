package ir.daneshrefah.scm.process.input;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.task.TaskCompleteRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskFindRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskInfoResponse;
import ir.daneshrefah.scm.process.service.task.TaskService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_CARTABLE_GET_ALL_TASK;
import static ir.daneshrefah.scm.common.constant.OperationCode.SVC_COMPLETE_TASK;
import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_ADMIN_BPM;

@Service
public class TaskManagementService extends AbstractJavaService {

    private final TaskService taskService;

    public TaskManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, TaskService taskService) {
        super(producerTemplate, objectMapper);
        this.taskService = taskService;
    }

    //    @PreAuthorize("isFullyAuthenticated()")
    @JavaService(operationCode = SVC_CARTABLE_GET_ALL_TASK)
    public PagedResponseData<TaskInfoResponse> findTaskList(TaskFindRequest taskFindRequest) throws Exception {
        if (Objects.isNull(taskFindRequest)) {
            taskFindRequest = new TaskFindRequest();
        }
        UserAuthentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        assert authentication != null;
        String assignee = authentication.getPrincipal().getPerson().getUsername();
        boolean isAdmin = authentication.hasAuthority(ROLE_ADMIN_BPM);
//        if (StringUtils.isBlank(taskFindRequest.getAssignee())) {
//            if (isAdmin) {
//                taskFindRequest.setAssignee(assignee);
//            } else {
//                throw new InvalidAssigneeException("assigne", "Invalid assignee: Either the assignee is blank or does not match for a non-admin user.");
//            }
//        } else if (!taskFindRequest.getAssignee().equals(assignee) && !isAdmin) {
//            throw new InvalidAssigneeException("assigne", "Invalid assignee: Either the assignee is blank or does not match for a non-admin user.");
//        }
        return taskService.findTaskList(taskFindRequest);
    }

    @JavaService(operationCode = SVC_COMPLETE_TASK)
    public boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException {
        return taskService.completeTask(taskRequest);
    }
}
