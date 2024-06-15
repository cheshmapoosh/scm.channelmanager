package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.process.exception.TaskAssignmentException;
import ir.daneshrefah.scm.process.exception.TaskNotFoundException;
import ir.daneshrefah.scm.process.model.constant.UserTaskStatus;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.model.request.TaskRequest;
import ir.daneshrefah.scm.process.model.response.ProcessResponse;
import ir.daneshrefah.scm.process.model.response.TaskResponse;
import ir.daneshrefah.scm.process.model.task.Assignment;
import ir.daneshrefah.scm.process.model.task.TaskInfo;
import ir.daneshrefah.scm.process.model.task.TaskMetadata;
import ir.daneshrefah.scm.process.service.dto.TaskFindRequest;
import ir.daneshrefah.scm.process.service.process.CamundaProcessService;
import ir.daneshrefah.scm.process.service.util.CamundaProcessUtil;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CamundaTaskService implements TaskService {

    public static final String ACTIONS = "actions";
    public static final String EXTRACT = "extract";
    private final PersonService personService;
    private final org.camunda.bpm.engine.TaskService taskService;
    private final RuntimeService runtimeService;
    private final RepositoryService repositoryService;
    private final CamundaProcessUtil camundaProcessUtil;

//    @Autowired
//    private PersonService personService;


    public List<TaskResponse> findTaskList(TaskFindRequest taskFindRequest) throws JsonProcessingException {
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(taskFindRequest.getAssignee());
        if (StringUtils.isNotBlank(taskFindRequest.getTaskId())) {
            taskQuery.taskId(taskFindRequest.getTaskId());
        }
        List<Task> tasks = taskQuery.list();
        List<TaskInfo> taskInfoList = tasks.stream().map(task -> {
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
                TaskMetadata metadata = extractTaskMetadata(task);
                taskInfo.setMetadata(metadata);
            }

            ProcessInstanceInfo processInstance = new ProcessInstanceInfo();
            processInstance.setProcessInstanceId(task.getProcessInstanceId());
            processInstance.setProcessDefinitionId(task.getProcessDefinitionId());
            taskInfo.setProcessInstance(processInstance);

            return taskInfo;
        }).collect(Collectors.toList());

        List<TaskResponse> taskResponses = new ArrayList<>();
        for (Task task : tasks) {
            TaskResponse taskResponse = new TaskResponse();
            ProcessResponse processResponse = new ProcessResponse();    //TODO use mapstruct
            taskResponse.setTaskId(task.getId());
            taskResponse.setTaskName(task.getName());
            taskResponse.setTaskPersianName(task.getTaskDefinitionKey()); //TODO use resource bundle
            taskResponse.setTaskDescription(task.getDescription());
            taskResponse.setTaskDefinitionKey(taskResponse.getTaskDefinitionKey());
//            IndividualPersonEntity personAssignee = personServiceDatabaseImpl.findPersonByNationalCode(task.getAssignee());
//            taskResponse.setAssignment(List.of(findPerson(task.getAssignee())));
            taskResponse.setCreateTime(task.getCreateTime());
            taskResponse.setCreateTimeMillis(task.getCreateTime().getTime());
            taskResponse.setDescription(task.getDescription());
            processResponse.setProcessInstanceId(task.getProcessInstanceId());
            processResponse.setProcessDefinitionId(task.getProcessDefinitionId());
            processResponse.setExecutionId(task.getExecutionId());
            String processDefinitionId = StringUtils.substringBefore(task.getProcessDefinitionId(), ":");
            processResponse.setProcessName(processDefinitionId); //TODO use resource bundle
            taskResponse.setProcess(processResponse);
            taskResponse.setUserTaskStatus(UserTaskStatus.WAITING);
            Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(task);
            Map<String, Object> processData = getProcessData(extensionProperties, task);
            taskResponse.setData(processData);
            setActions(processData, extensionProperties);
            taskResponses.add(taskResponse);
        }
        return taskResponses;
    }

    private TaskMetadata extractTaskMetadata(Task task) {
        TaskMetadata metadata = new TaskMetadata();
        Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(task);
        //TODO
        return metadata;
    }

    public IndividualPersonEntity findPerson(String nationalCode) throws JsonProcessingException {
//        if (nationalCode == null) {
//            return null;
//        }
//        IndividualPersonEntity person = personService.findPersonByNationalCode(nationalCode);
//        if (person != null) {
//            return person;
//        }
//        //TODO How Call cif
        return null;
    }

    private static void setActions(Map<String, Object> processData, Map<String, String> extensionProperties) throws JsonProcessingException {
        String actions = extensionProperties.get(ACTIONS);
        if (actions != null && !actions.trim().isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            processData.put(ACTIONS, objectMapper.readTree(actions));
        }
    }

    private Map<String, Object> getProcessData(Map<String, String> extensionProperties, Task task) {
        String extract = extensionProperties.get(EXTRACT);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> businessDataMap = new HashMap<>();
        if (extract != null) {
            for (String extractValue : extract.split(",")) {
                if (extractValue.startsWith("%s_".formatted(CamundaProcessService.BUSINESS_DATA))) {
                    Map<String, Object> variableMap = taskService.getVariables(task.getId());
                    if (variableMap.containsKey(extractValue)) {
                        String value = extractValue.replace("%s_".formatted(CamundaProcessService.BUSINESS_DATA), "");
                        businessDataMap.put(value, variableMap.get(extractValue));
                    }
                } else {
                    data.put(extractValue, taskService.getVariable(task.getId(), extractValue));
                }
            }
            data.put(CamundaProcessService.BUSINESS_DATA, businessDataMap);
        }
        return data;
    }

    public boolean completeTask(TaskRequest taskRequest) throws JsonProcessingException {
        Task task = findByTaskID(taskRequest);
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

    //TODO move this method to camunda util and change the name of method
    private Task findByTaskID(TaskRequest taskRequest) {
        String loggedInUserId = taskRequest.getNationalCode();
        Task task = taskService.createTaskQuery().taskId(taskRequest.getTaskId()).taskAssignee(loggedInUserId).singleResult();
        if (task == null) {
            throw new TaskNotFoundException("CamundaTaskService", "Task with ID " + taskRequest.getTaskId() + " not found for assignee " + loggedInUserId);
        }
        if (!task.getAssignee().equals(loggedInUserId)) {
            throw new TaskAssignmentException("CamundaTaskService", "Task with ID " + taskRequest.getTaskId() + " is not assigned to user " + loggedInUserId);
        }
        return task;
    }
}
