package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.entity.TaskCompleteLog;
import ir.daneshrefah.scm.process.exception.schema.JsonSchemaException;
import ir.daneshrefah.scm.process.exception.task.TaskNotFoundException;
import ir.daneshrefah.scm.process.exception.task.UnauthorizedCompleteTaskException;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.model.task.Assignment;
import ir.daneshrefah.scm.process.model.task.TaskMetadata;
import ir.daneshrefah.scm.process.service.dto.task.TaskCompleteRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskFindRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskInfoResponse;
import ir.daneshrefah.scm.process.service.util.BpmnExtensionExtractor;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
import ir.daneshrefah.scm.service.TaskCompleteLogService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_ADMIN_BPM;
import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.*;

@Service
@RequiredArgsConstructor
public class CamundaTaskService implements TaskService {
    private final PersonService personService;
    private final org.camunda.bpm.engine.TaskService taskService;
    private final BpmnExtensionExtractor bpmnExtensionExtractor;
    private final ResourceBundleService resourceBundleService;
    private final TaskCompleteLogService taskCompleteLogService;
    private final ObjectMapper objectMapper;


    public PagedResponseData<TaskInfoResponse> findTaskList(TaskFindRequest taskFindRequest) {
        Locale locale = AccessibleLocale.FA_IR.getLocale();//TODO get local from header
        TaskQuery taskQuery = taskService.createTaskQuery().taskAssignee(taskFindRequest.getAssignee());
        if (StringUtils.isNotBlank(taskFindRequest.getTaskId())) {
            taskQuery.taskId(taskFindRequest.getTaskId());
        }
        int firstResult = (taskFindRequest.getPageNo() - 1) * taskFindRequest.getPageSize();
        int maxResult = Math.max((taskFindRequest.getPageNo() * taskFindRequest.getPageSize()) - 1, taskFindRequest.getPageSize());
        long count = taskQuery.count();
        List<Task> tasks = taskQuery.listPage(firstResult, maxResult);
        List<TaskInfoResponse> taskInfoResponses = tasks.stream().map(task -> {
            TaskInfoResponse taskInfoResponse = new TaskInfoResponse();
            taskInfoResponse.setTaskId(task.getId());
            taskInfoResponse.setName(resourceBundleService.get(locale, task.getName()).orElse(task.getName()));
            taskInfoResponse.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().getTime() : null);
            Map<String, Object> businessData = getBusinessData(task);
            taskInfoResponse.setData(businessData);
            setActions(businessData, task);
            Assignment assignment = new Assignment();
            assignment.setUsername(task.getAssignee());
            if (taskFindRequest.isIncludePersonInfo()) {
                GeneralRealPerson person = personService.findPersonByNationalCode(assignment.getUsername());
                if (person != null) {
                    assignment.setFirstName(person.getFirstName());
                    assignment.setLastName(person.getLastName());
                    assignment.setFirstNameEnglish(person.getFirstNameEnglish());
                    assignment.setLastNameEnglish(person.getLastNameEnglish());
                    assignment.setNationalCode(person.getNationalCode());
                }
            }
            taskInfoResponse.setAssignments(List.of(assignment));
            if (taskFindRequest.isIncludeMetadata()) {
                TaskMetadata metadata = getTaskMetadata(task);
                taskInfoResponse.setMetadata(metadata);
            }
            ProcessInstanceInfo processInstance = new ProcessInstanceInfo();
            processInstance.setProcessInstanceId(task.getProcessInstanceId());
            processInstance.setProcessDefinitionId(StringUtils.substringBefore(task.getProcessDefinitionId(), ":"));
            taskInfoResponse.setProcessInstance(processInstance);
            return taskInfoResponse;
        }).collect(Collectors.toList());
        return new PagedResponseData<>(taskFindRequest.getPageNo(), taskFindRequest.getPageSize(), count, taskInfoResponses);
    }

    /**
     * Retrieves action that is appended to the businessData map if defined in extension properties.
     * This extension property is defined in the task extension properties in Camunda Modeler.
     * The extension property name must be "actions" and its value should be a JSON array
     * like [{"name": "امضا", "action": "accept"}, {"name": "رد امضا", "action": "reject"}].
     *
     * @param businessData the map to append the actions to
     * @param task         the task to extract actions from
     */
    @SneakyThrows
    private void setActions(Map<String, Object> businessData, Task task) {
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        String actions = extensionProperties.get(ACTIONS);
        if (actions != null && !actions.trim().isEmpty()) {
            businessData.put(ACTIONS, objectMapper.readTree(actions));
        }
    }

    /**
     * Retrieves business data and task variables based on extension properties.
     * This extension property is defined in the task extension properties in Camunda Modeler.
     * The extension property name must be "extension" and its value should be a comma-separated list
     * like "businessData_transactionData,businessData_transactionType,issuerInfo,message".
     * For example, "transactionData" is data that the process started with, and before the process started,
     * the "businessData_" prefix was appended to that data. "message" is a variable that, if attached to the task,
     * must be appended to the business data without the "businessData_" prefix because the process did not start with it.
     *
     * @param task the task to extract data
     * @return a map containing business data and other task variables
     */
    private Map<String, Object> getBusinessData(Task task) {
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        String extension = extensionProperties.get(EXTENSION);
        Map<String, Object> data = new HashMap<>();
        if (extension != null) {
            Map<String, Object> variableMap = taskService.getVariables(task.getId());
            Map<String, Object> businessDataMap = new HashMap<>();
            for (String extractValue : extension.split(",")) {
                if (extractValue.startsWith(BUSINESS_DATA + "_")) {
                    if (variableMap.containsKey(extractValue)) {
                        String key = extractValue.replace(BUSINESS_DATA + "_", "");
                        businessDataMap.put(key, variableMap.get(extractValue));
                    }
                } else {
                    data.put(extractValue, variableMap.get(extractValue));
                }
            }
            data.put(BUSINESS_DATA, businessDataMap);
        }
        return data;
    }

    /**
     * Retrieves metadata for a task based on extension properties.
     * Configure actions, output variables, and validation schema if defined.
     *
     * @param task the task to retrieve metadata from
     * @return taskMetadata object containing configured actions, output variables, and validation schema
     */
    @SneakyThrows
    private TaskMetadata getTaskMetadata(Task task) {
        TaskMetadata metadata = new TaskMetadata();
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        setActionMetaData(metadata, extensionProperties);
        setOutputVariables(metadata.getOutputVariables(), extensionProperties);
        setValidationSchema(metadata, extensionProperties);
        return metadata;
    }

    /**
     * Sets action metadata in the provided TaskMetadata object based on extension properties.
     */
    private void setActionMetaData(TaskMetadata metadata, Map<String, String> extensionProperties) {
        String properties = extensionProperties.get(ACTIONS);
        if (StringUtils.isNotEmpty(properties)) {
            for (String property : properties.split(",")) {
                if (StringUtils.isNotEmpty(extensionProperties.get(property))) {
                    metadata.getActions().add(extensionProperties.get(property));
                }
            }
        }
    }

    /**
     * Sets output variables in the provided list based on extension properties.
     */
    private void setOutputVariables(List<String> outputVariables, Map<String, String> extensionProperties) {
        String properties = extensionProperties.get(OUTPUT_VARIABLES);
        if (StringUtils.isNotEmpty(properties)) {
            Collections.addAll(outputVariables, properties.split(","));
        }
    }

    /**
     * Sets validation schema in the provided TaskMetadata object based on extension properties.
     *
     * @throws JsonProcessingException if there is an error processing the JSON schema
     */
    private void setValidationSchema(TaskMetadata metadata, Map<String, String> extensionProperties) throws JsonProcessingException {
        String properties = extensionProperties.get(JSON_SCHEMA);
        if (StringUtils.isNotEmpty(properties)) {
            metadata.setValidationSchema(ValidationSchema.getJsonNode(properties));
        }
    }

    public boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException {
        TaskCompleteLog taskCompleteLog = new TaskCompleteLog();
        try {
            String locale = "fa-IR";//TODO how get this locale from header
            Task task = findTaskById(taskRequest.getTaskId());
            if (!checkTaskAssignment(task)) {
                throw new UnauthorizedCompleteTaskException(task.getId(), "Unauthorized to complete the task");
            }
            TaskMetadata metadata = getTaskMetadata(task);
            if (metadata.getValidationSchema() != null && !metadata.getValidationSchema().isEmpty()) {
                Set<ValidationMessage> validationMessages = ValidationSchema.validate(taskRequest, metadata.getValidationSchema(), locale);
                if (validationMessages != null && !validationMessages.isEmpty()) {
                    List<String> messageExceptions = validationMessages.stream().map(ValidationMessage::getMessage).toList();
                    throw new JsonSchemaException("Input", String.join("\n", messageExceptions));
                }
            }
            if (taskRequest.getAction() != null && !taskRequest.getAction().isEmpty()) {
                taskService.setVariable(task.getId(), ACTION, taskRequest.getAction());
            }
            taskService.complete(task.getId());
            taskCompleteLog.setStatus("COMPLETE");

        } catch (Exception e) {
            taskCompleteLog.setStatus("FAILED");
            taskCompleteLog.setExceptionClassName(e.getClass().getName());
            throw e;
        } finally {
            fillTaskCompleteLog(taskRequest, taskCompleteLog);
            taskCompleteLogService.save(taskCompleteLog);
        }
        return true;
    }

    private  void fillTaskCompleteLog(TaskCompleteRequest taskFindRequest, TaskCompleteLog taskCompleteLog) throws JsonProcessingException {
        UserAuthentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        taskCompleteLog.setCompleteAt(new Date());
        taskCompleteLog.setTaskId(taskFindRequest.getTaskId());
        assert authentication != null;
        taskCompleteLog.setIssuerUserName(authentication.getPrincipal().getPerson().getUsername());
        taskCompleteLog.setPayload(objectMapper.writeValueAsString(taskFindRequest));
    }


    private boolean checkTaskAssignment(Task task) {
        UserAuthentication authentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.isNull(authentication)) {
            return false;
        }
        String assignee = task.getAssignee();//TODO remove it
//        String assignee = authentication.getPrincipal().getPerson().getUsername();
        return authentication.hasAuthority(ROLE_ADMIN_BPM) || authentication.getPrincipal().getPerson().getUsername().equals(assignee);
    }

    private Task findTaskById(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new TaskNotFoundException(taskId, "Task not found.");
        }
        return task;
    }
}
