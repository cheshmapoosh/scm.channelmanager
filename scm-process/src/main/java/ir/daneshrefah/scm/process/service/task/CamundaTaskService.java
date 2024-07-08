package ir.daneshrefah.scm.process.service.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.data.service.bundle.ResourceBundleService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.exception.schema.JsonSchemaException;
import ir.daneshrefah.scm.process.exception.task.TaskNotFoundException;
import ir.daneshrefah.scm.process.exception.task.UnauthorizedCompleteTaskException;
import ir.daneshrefah.scm.process.model.process.ProcessInstanceInfo;
import ir.daneshrefah.scm.process.model.task.Assignment;
import ir.daneshrefah.scm.process.service.dto.task.TaskInfoResponse;
import ir.daneshrefah.scm.process.model.task.TaskMetadata;
import ir.daneshrefah.scm.process.service.dto.task.TaskCompleteRequest;
import ir.daneshrefah.scm.process.service.dto.task.TaskFindRequest;
import ir.daneshrefah.scm.process.service.util.BpmnExtensionExtractor;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
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

    public PagedResponseData<TaskInfoResponse> findTaskList(TaskFindRequest taskFindRequest) {
        Locale locale = AccessibleLocale.FA_IR.getLocale();//TODO get local from header
        TaskQuery taskQuery = taskService
                .createTaskQuery()
                .taskAssignee(taskFindRequest.getAssignee());
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
            taskInfoResponse.setName(resourceBundleService.get(locale, task.getName()).orElse(task.getName())); //TODO resourceBundleService.get()
            taskInfoResponse.setCreateTime(task.getCreateTime() != null ? task.getCreateTime().getTime() : null);
            Map<String, Object> businessData = getBusinessData(task);
            taskInfoResponse.setData(businessData);
            Assignment assignment = new Assignment();
            assignment.setUsername(task.getAssignee());
            setActions(businessData, task);
            if (taskFindRequest.isIncludePersonInfo()) {
                GeneralRealPersonEntity person = personService.findPersonByNationalCode(assignment.getUsername());
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

    @SneakyThrows
    private void setActions(Map<String, Object> businessData, Task task) {
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        String actions = extensionProperties.get(ACTIONS);
        if (actions != null && !actions.trim().isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            businessData.put(ACTIONS, objectMapper.readTree(actions));
        }
    }

    private Map<String, Object> getBusinessData(Task task) {
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        String extension = extensionProperties.get(EXTENSION);
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> businessDataMap = new HashMap<>();
        if (extension != null) {
            for (String extractValue : extension.split(",")) {
                if (extractValue.startsWith("%s_".formatted(BUSINESS_DATA))) {
                    Map<String, Object> variableMap = taskService.getVariables(task.getId());
                    if (variableMap.containsKey(extractValue)) {
                        String value = extractValue.replace("%s_".formatted(BUSINESS_DATA), "");
                        businessDataMap.put(value, variableMap.get(extractValue));
                    }
                } else {
                    data.put(extractValue, taskService.getVariable(task.getId(), extractValue));
                }
            }
            data.put(BUSINESS_DATA, businessDataMap);
        }
        return data;
    }

    @SneakyThrows
    private TaskMetadata getTaskMetadata(Task task) {
        TaskMetadata metadata = new TaskMetadata();
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        setActionMetaData(metadata, extensionProperties);
        setOutputVariables(metadata.getOutputVariables(), extensionProperties);
        setValidationSchema(metadata, extensionProperties);
        return metadata;
    }
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
    private void setOutputVariables(List<String> outputVariables, Map<String, String> extensionProperties) {
        String properties = extensionProperties.get(OUTPUT_VARIABLES);
        if (StringUtils.isNotEmpty(properties)) {
            Collections.addAll(outputVariables, properties.split(","));
        }
    }

    private void setValidationSchema(TaskMetadata metadata, Map<String, String> extensionProperties) {
        String properties = extensionProperties.get(JSON_SCHEMA);
        if (StringUtils.isNotEmpty(properties)) {
            metadata.setValidationSchema(properties);
        }
    }

    public boolean completeTask(TaskCompleteRequest taskRequest) throws JsonProcessingException {
        String locale = "fa-IR";//TODO how get this locale from header
        Task task = findTaskById(taskRequest.getTaskId());
        if (!checkTaskAssignment(task)) {
            throw new UnauthorizedCompleteTaskException(task.getId(),"Unauthorized to complete the task");
        }
        TaskMetadata metadata = getTaskMetadata(task);
        if (metadata.getValidationSchema() != null && !metadata.getValidationSchema().isEmpty()) {
            Set<ValidationMessage> validationMessages = ValidationSchema.validate(taskRequest, metadata.getValidationSchema(),locale);
            if (validationMessages != null && !validationMessages.isEmpty()) {
                List<String> messageExceptions = validationMessages.stream().map(ValidationMessage::getMessage).toList();
                throw new JsonSchemaException("Input", String.join("\n", messageExceptions));
            }
        }
        if (taskRequest.getAction() != null && !taskRequest.getAction().isEmpty()) {
            taskService.setVariable(task.getId(), ACTION, taskRequest.getAction());
        }
        taskService.complete(task.getId());
        return true;
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
