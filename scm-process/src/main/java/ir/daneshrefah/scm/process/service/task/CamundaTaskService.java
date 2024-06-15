package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.process.exception.TaskNotFoundException;
import ir.daneshrefah.scm.process.model.constant.UserTaskStatus;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.service.dto.TaskCompleteRequest;
import ir.daneshrefah.scm.process.model.response.ProcessResponse;
import ir.daneshrefah.scm.process.model.response.TaskResponse;
import ir.daneshrefah.scm.process.model.task.Assignment;
import ir.daneshrefah.scm.process.model.task.TaskInfo;
import ir.daneshrefah.scm.process.model.task.TaskMetadata;
import ir.daneshrefah.scm.process.service.dto.TaskFindRequest;
import ir.daneshrefah.scm.process.service.process.CamundaProcessService;
import ir.daneshrefah.scm.process.service.util.CamundaProcessUtil;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_ADMIN_BPM;

@RequiredArgsConstructor
@Service
public class CamundaTaskService implements TaskService {

    public static final String EXTENSION = "extension";
    public static final String OUTPUT_VARIABLES = "outputVariables";

    private final PersonService personService;
    private final org.camunda.bpm.engine.TaskService taskService;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final CamundaProcessUtil camundaProcessUtil;


    public List<TaskInfo> findTaskList(TaskFindRequest taskFindRequest) throws JsonProcessingException {
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(taskFindRequest.getAssignee());
        if (StringUtils.isNotBlank(taskFindRequest.getTaskId())) {
            taskQuery.taskId(taskFindRequest.getTaskId());
        }
        List<Task> tasks = taskQuery.listPage(taskFindRequest.getPageNo(), taskFindRequest.getPageSize());
        return tasks.stream().map(task -> {
            TaskInfo taskInfo = new TaskInfo();
            taskInfo.setTaskId(task.getId());
            taskInfo.setName(task.getName());
            Assignment assignment = new Assignment();
            assignment.setUsername(task.getAssignee());
            if (taskFindRequest.isIncludePersonInfo()) {
                Optional<GeneralPerson> person = personService.findPersonByPersonUsername(assignment.getUsername());
            }
            taskInfo.setAssignments(List.of(assignment));
            if (taskFindRequest.isIncludeMetadata()) {
                TaskMetadata metadata = getTaskMetadata(task);
                taskInfo.setMetadata(metadata);
            }
            ProcessInstanceInfo processInstance = new ProcessInstanceInfo();
            processInstance.setProcessInstanceId(task.getProcessInstanceId());
            processInstance.setProcessDefinitionId(task.getProcessDefinitionId());
            taskInfo.setProcessInstance(processInstance);
            return taskInfo;
        }).collect(Collectors.toList());

//        List<TaskResponse> taskResponses = new ArrayList<>();
//        for (Task task : tasks) {
//            TaskResponse taskResponse = new TaskResponse();
//            ProcessResponse processResponse = new ProcessResponse();    //TODO use mapstruct
//            taskResponse.setTaskId(task.getId());
//            taskResponse.setTaskName(task.getName());
//            taskResponse.setTaskPersianName(task.getTaskDefinitionKey()); //TODO use resource bundle
//            taskResponse.setTaskDescription(task.getDescription());
//            taskResponse.setTaskDefinitionKey(taskResponse.getTaskDefinitionKey());
//            taskResponse.setAssignment(findPerson(task.getAssignee()));
//            taskResponse.setCreateTime(task.getCreateTime());
//            taskResponse.setCreateTimeMillis(task.getCreateTime().getTime());
//            taskResponse.setDescription(task.getDescription());
//            processResponse.setProcessInstanceId(task.getProcessInstanceId());
//            processResponse.setProcessDefinitionId(task.getProcessDefinitionId());
//            processResponse.setExecutionId(task.getExecutionId());
//            String processDefinitionId = StringUtils.substringBefore(task.getProcessDefinitionId(), ":");
//            processResponse.setProcessName(processDefinitionId); //TODO use resource bundle
//            taskResponse.setProcess(processResponse);
//            taskResponse.setUserTaskStatus(UserTaskStatus.WAITING);
//            Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(task);
//            Map<String, Object> processData = getProcessData(extensionProperties, task);
//            taskResponse.setData(processData);
//            setActions(processData, extensionProperties);
//            taskResponses.add(taskResponse);
//        }
//        return taskResponses;
    }

    @SneakyThrows
    private TaskMetadata getTaskMetadata(Task task) {
        TaskMetadata metadata = new TaskMetadata();
        Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(task);
        setExtensions(metadata.getExtension(), extensionProperties);
        setOutputVariables(metadata.getOutputVariables(), extensionProperties, task);
        return metadata;
    }


    private void setExtensions(Map<String, Object> extension, Map<String, String> extensionProperties) throws JsonProcessingException {
        String properties = extensionProperties.get(EXTENSION);
        for (String property : properties.split(",")) {
            if (StringUtils.isNotEmpty(extensionProperties.get(property))) {
                ObjectMapper objectMapper = new ObjectMapper();
                extension.put(property, objectMapper.readTree(extensionProperties.get(property)));
            }
        }
    }

    private void setOutputVariables(Map<String, Object> outputVariables, Map<String, String> extensionProperties, Task task) {
        String properties = extensionProperties.get(OUTPUT_VARIABLES);
        for (String extractValue : properties.split(",")) {
            Map<String, Object> variableMap = taskService.getVariables(task.getId());
            if (variableMap.containsKey(extractValue)) {
                String value = extractValue.replace("%s_".formatted(CamundaProcessService.BUSINESS_DATA), "");
                outputVariables.put(value, variableMap.get(extractValue));
            } else {
                outputVariables.put(extractValue, variableMap.get(extractValue));
            }
        }
    }

    public boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException {
        Task task = findTaskById(taskRequest.getTaskId());
        if (!checkTaskAssignment(task)) {
//TODO            throw new
        }
        TaskMetadata metadata = getTaskMetadata(task);
        if (StringUtils.isNotBlank(metadata.getValidationSchema())) {
            ValidationSchema.validate(taskRequest.getData(), metadata.getValidationSchema());
        }
//        if (CollectionUtils.isNotEmpty(metadata.getActions())) {
//TODO            ValidationUtils.checkBlankString(taskRequest.getAction(), () -> new );
//TODO            ValidationUtils.checkListIsNotEmptyAndNotContains(metadata.getActions(), taskRequest.getAction(), () -> );
//        }

        Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(task);
        if (extensionProperties != null && !extensionProperties.isEmpty() && extensionProperties.containsKey("jsonSchema")) {
            ValidationSchema.validate(taskRequest, extensionProperties.get("jsonSchema"));
        }
        if (taskRequest.getAction() != null && !taskRequest.getAction().isEmpty()) {
            taskService.setVariable(task.getId(), taskRequest.getAction(), taskRequest.getAction());
        }
        taskService.complete(task.getId());
        return true;
    }

    private boolean checkTaskAssignment(Task task) {
        UserAuthentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }
        String assignee = task.getAssignee();
        return authentication.hasAuthority(ROLE_ADMIN_BPM) ||
                authentication.getPrincipal().getPerson().getUsername().equals(assignee);
    }

    private Task findTaskById(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new TaskNotFoundException("CamundaTaskService", "Task with ID " + taskId + " not found.");
        }
        return task;
    }

}
