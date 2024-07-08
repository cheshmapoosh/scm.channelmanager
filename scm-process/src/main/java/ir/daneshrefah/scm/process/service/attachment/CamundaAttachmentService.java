package ir.daneshrefah.scm.process.service.attachment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.process.exception.attachment.AttachmentPermissionException;
import ir.daneshrefah.scm.process.exception.common.UnauthorizedException;
import ir.daneshrefah.scm.process.exception.task.TaskNotFoundException;
import ir.daneshrefah.scm.process.model.attachment.AttachmentInfo;
import ir.daneshrefah.scm.process.model.attachment.AttachmentProcessInfo;
import ir.daneshrefah.scm.process.model.attachment.AttachmentTaskInfo;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.ProcessDeleteAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskAttachmentRequest;
import ir.daneshrefah.scm.process.service.dto.attachment.TaskDeleteAttachmentRequest;
import ir.daneshrefah.scm.process.service.util.BpmnExtensionExtractor;
import ir.daneshrefah.scm.process.service.util.ValidateUserService;
import ir.daneshrefah.scm.process.service.util.processVariable.ProcessVariableService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.ATTACHMENT;
import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.PROCESS_ATTACHMENT;

@Service
@RequiredArgsConstructor
public class CamundaAttachmentService implements AttachmentService {

    private final TaskService taskService;
    private final RuntimeService runtimeService;
    private final BpmnExtensionExtractor bpmnExtensionExtractor;
    private final ProcessVariableService processVariableService;
    private final ValidateUserService validateUserService;

    @Override
    public void taskAttachment(TaskAttachmentRequest taskAttachmentRequest) throws Exception {
        String username = taskAttachmentRequest.getAssignee();//TODO change it with token;
        Task task = getTask(taskAttachmentRequest.getTaskId(), username);
        validateAttachments(taskAttachmentRequest.getData(), getAttachmentInfos(task).getAttachmentInfos());
        taskService.setVariablesLocal(taskAttachmentRequest.getTaskId(), taskAttachmentRequest.getData());
    }

    @Override
    public void deleteTaskAttachment(TaskDeleteAttachmentRequest taskDeleteAttachmentRequest) throws Exception {
        String username = taskDeleteAttachmentRequest.getAssignee();//TODO change it with token;
        Task task = getTask(taskDeleteAttachmentRequest.getTaskId(), username);
        validateAndDeletionsVariable(task, taskDeleteAttachmentRequest.getDeletes(), getAttachmentInfos(task));
    }

    @Override
    public void processAttachment(ProcessAttachmentRequest processAttachmentRequest) throws Exception {
        ProcessInstance processInstance = getProcessInstance(processAttachmentRequest.getProcessId());
        AttachmentProcessInfo attachmentProcessInfo = getProcessAttachmentInfos(processAttachmentRequest.getProcessId());
        validateAttachments(processAttachmentRequest.getData(), attachmentProcessInfo.getAttachmentInfos());
        if (isUserAuthorized(attachmentProcessInfo, processInstance)) {
            processVariableService.setProcessVariables(processInstance.getId(), processAttachmentRequest.getData());
        } else {
            throw new UnauthorizedException("", "User is not authorized to process attachment");//TODO change it
        }
    }

    public void deleteProcessAttachment(ProcessDeleteAttachmentRequest processDeleteAttachmentRequest) throws Exception {
        ProcessInstance processInstance = getProcessInstance(processDeleteAttachmentRequest.getProcessID());
        AttachmentProcessInfo attachmentProcessInfo = getProcessAttachmentInfos(processInstance.getId());
        for (AttachmentInfo attachmentInfo : attachmentProcessInfo.getAttachmentInfos()) {
            boolean isValid = false;
            for (String variableName : processDeleteAttachmentRequest.getDeletes()) {
                if (attachmentInfo.getAttachmentName().equalsIgnoreCase(variableName)) {
                    if (isUserAuthorized(attachmentProcessInfo, processInstance)) {
                        runtimeService.removeVariable(processInstance.getId(), variableName);
                    } else {
                        throw new UnauthorizedException("", "User is not authorized to process attachment");//TODO change it
                    }
                    isValid = true;
                }
                if (!isValid) {
                    throw new AttachmentPermissionException(variableName, "You do not have permission to :operation this attachment = :attachment" + variableName, "remove", variableName);
                }
            }
        }
    }

    private void validateAttachments(Map<String, Object> data, List<AttachmentInfo> attachmentInfos) throws Exception {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            boolean isValid = false;
            for (AttachmentInfo attachmentInfo : attachmentInfos) {
                if (entry.getKey().equals(attachmentInfo.getAttachmentName())) {
                    validateAttachmentType(entry.getValue(), attachmentInfo.getType());
                    isValid = true;
                }
            }
            if (!isValid) {
                throw new AttachmentPermissionException(entry.getKey(), "You do not have permission to :operation this attachment = :attachment" + entry.getKey(), "Add", entry.getKey());
            }
        }
    }

    private void validateAttachmentType(Object attachmentObject, String type) throws ClassNotFoundException {
        Class<?> typeClass = getTypeClass(type);
        if (!typeClass.isInstance(attachmentObject)) {
            throw new InvalidInputException(attachmentObject.getClass().getTypeName());
        }
    }

    private Class<?> getTypeClass(String type) throws ClassNotFoundException {
        return switch (type.toLowerCase()) {
            case "string" -> Class.forName("java.lang.String");
            case "integer" -> Class.forName("java.lang.Integer");
            case "double" -> Class.forName("java.lang.Double");
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    private void validateAndDeletionsVariable(Task task, List<String> deletes, AttachmentTaskInfo attachmentTaskInfo) throws AttachmentPermissionException {
        for (String delete : deletes) {
            if (attachmentTaskInfo.getDeletes() != null && attachmentTaskInfo.getDeletes().contains(delete)) {
                taskService.removeVariable(task.getId(), delete);
            } else {
                throw new AttachmentPermissionException(delete, "You do not have permission to :operation this attachment = :attachment" + delete, "remove", delete);
            }
        }
    }

    private ProcessInstance getProcessInstance(String processId) throws TaskNotFoundException {
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processId).singleResult();
        if (processInstance == null) {
            throw new TaskNotFoundException(processId, "Process instance not found");
        }
        return processInstance;
    }

    private Task getTask(String taskId, String assignee) throws TaskNotFoundException {
        Task task = taskService.createTaskQuery().taskId(taskId).taskAssignee(assignee).singleResult();
        if (task == null) {
            throw new TaskNotFoundException(taskId, "Task not found");
        }
        return task;
    }
    private AttachmentProcessInfo getProcessAttachmentInfos(String processId) throws JsonProcessingException {
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(processId, Collaboration.class);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(extensionProperties.get(PROCESS_ATTACHMENT), new TypeReference<>() {
        });
    }

    private boolean isUserAuthorized(AttachmentProcessInfo attachmentProcessInfo, ProcessInstance processInstance) {
        String userName = Objects.requireNonNull(AuthenticationUtils.getLoggedInUser()).getPerson().getUsername();
        return validateUserService.isUserInAuthorizedRoles(attachmentProcessInfo.getRoles())
                || validateUserService.isUserAuthorized(
                processVariableService.getProcessVariables(processInstance.getId()),
                userName,
                attachmentProcessInfo.getPermissions()
        );
    }

    private AttachmentTaskInfo getAttachmentInfos(Task task) throws JsonProcessingException {
        Map<String, String> extensionProperties = bpmnExtensionExtractor.getExtensionProperties(task);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(extensionProperties.get(ATTACHMENT), new TypeReference<>() {
        });
    }
}
