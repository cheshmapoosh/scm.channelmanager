package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.process.exception.TaskAssignmentException;
import ir.daneshrefah.scm.process.exception.TaskNotFoundException;
import ir.daneshrefah.scm.process.model.constant.UserTaskStatus;
import ir.daneshrefah.scm.process.model.request.TaskRequest;
import ir.daneshrefah.scm.process.model.response.ProcessResponse;
import ir.daneshrefah.scm.process.model.response.TaskResponse;
import ir.daneshrefah.scm.process.service.ProcessBundleService;
import ir.daneshrefah.scm.process.service.process.CamundaProcessService;
import ir.daneshrefah.scm.process.service.util.CamundaProcessUtil;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CamundaTaskService implements TaskManagement {

    public static final String ACTIONS = "actions";
    public static final String EXTRACT = "extract";
    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ProcessBundleService processBundleService;

    @Autowired
    private CamundaProcessUtil camundaProcessUtil;

//    @Autowired
//    private PersonService personService;


    public List<TaskResponse> getTaskList(TaskRequest taskRequest) throws JsonProcessingException {
//        String loggedInUserId = getLoggedInUserNationalCode();
        String loggedInUserId = taskRequest.getNationalCode();
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(loggedInUserId).list();
        List<TaskResponse> taskResponses = new ArrayList<>();
        for (Task task : tasks) {
            TaskResponse taskResponse = new TaskResponse();
            ProcessResponse processResponse = new ProcessResponse();    //TODO use mapstruct
            taskResponse.setTaskId(task.getId());
            taskResponse.setTaskName(task.getName());
            taskResponse.setTaskPersianName(processBundleService.getBundle(task.getTaskDefinitionKey()));
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
            processResponse.setProcessName(processBundleService.getBundle(processDefinitionId));
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
