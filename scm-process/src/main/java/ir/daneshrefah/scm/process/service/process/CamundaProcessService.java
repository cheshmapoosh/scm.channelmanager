package ir.daneshrefah.scm.process.service.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.exception.NotFountProcessException;
import ir.daneshrefah.scm.process.model.IssuerUser;
import ir.daneshrefah.scm.process.model.constant.ProcessState;
import ir.daneshrefah.scm.process.model.mapper.ProcessResponseMapper;
import ir.daneshrefah.scm.process.model.mapper.UserMapper;
import ir.daneshrefah.scm.process.model.request.CancelProcessRequest;
import ir.daneshrefah.scm.process.model.request.ProcessStartRequest;
import ir.daneshrefah.scm.process.model.response.ProcessResponse;
import ir.daneshrefah.scm.process.service.util.CamundaProcessUtil;
import ir.daneshrefah.scm.process.service.util.UserAuthUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.instance.Collaboration;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CamundaProcessService implements ProcessManagement {
    public static final String ISSUER_USER = "issuerUser";

    public static final String BUSINESS_DATA = "businessData";

    public static final String CANCEL_ACCESS = "cancelProcess";//TODO change name of variable

    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private CamundaProcessUtil camundaProcessUtil;

    @Autowired
    private TaskService taskService;

    @Autowired
    private IdentityService identityService;

    public ProcessResponse startProcess(ProcessStartRequest processRequest) throws JsonProcessingException {
        ProcessDefinition processDefinition = findProcessDefinitionByProcessKey(processRequest.getProcessKey());
        Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(processDefinition, StartEvent.class);

//        String userId = ProcessUtils.getLoggedInUserNationalCode();
//        Set<ValidationMessage> validationMessages = ValidationSchema.validate(processRequest, jsonSchema, "");
//        if (validationMessages != null && !validationMessages.isEmpty()) {
//            String message = validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\\n"));
//            throw new GeneralException("CamundaProcessService", message); //TODO change this exception after dariush task completed
//        }
//        String userId = "123";
//        identityService.setAuthenticatedUserId(userId);
        Map<String, Object> businessData = new HashMap<>();
        for (Map.Entry<String, Object> entry : processRequest.getData().entrySet()) {
            businessData.put(BUSINESS_DATA + "_" + entry.getKey(), entry.getValue());
        }
        IssuerUser issuerUser = UserMapper.INSTANCE.toUserMapper(UserAuthUtils.getLoggedInPerson());
        businessData.computeIfAbsent(ISSUER_USER, s -> issuerUser);
        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), businessData);
        return createResponse(processInstance);
    }

    public boolean cancelProcess(CancelProcessRequest cancelProcessRequest) throws Exception {
        //TODO Validate ProcessInstanceId not null with jsonSchema
        String processInstanceId = cancelProcessRequest.getProcessId();
//        String loggedInUserNationalCode = UserAuthUtils.getLoggedInUserNationalCode();
        String loggedInUserNationalCode = cancelProcessRequest.getNationalCode();

        ProcessInstance processInstance = camundaProcessUtil.getProcessInstance(processInstanceId);
        if (processInstance == null) {
            throw new Exception("No process instance found with ID: " + processInstanceId);// TODO change this exception
        }

        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);

        ProcessDefinition processDefinition = camundaProcessUtil.getProcessDefinition(processInstance.getProcessDefinitionId());

        //TODO clean code
        Map<String, String> extensionProperties = camundaProcessUtil.getExtensionProperties(processDefinition, Collaboration.class);
        boolean canCancel = false;
        if (extensionProperties.containsKey(CANCEL_ACCESS)) {
            String extension = extensionProperties.get(CANCEL_ACCESS);
            for (String extensionVariable : extension.split(",")) {
                if (variables.containsKey(extensionVariable)) {
                    Object user = variables.get(extensionVariable);
                    if (user instanceof String) {
                        if (((String) user).trim().equals(loggedInUserNationalCode)) {
                            canCancel = true;
                            break;
                        }
                    } else if (user instanceof List) {
                        List<String> users = (List<String>) user;
                        for (String u : users) {
                            if (u.trim().equals(loggedInUserNationalCode)) {
                                canCancel = true;
                                break;
                            }
                        }
                    } else if (user instanceof IssuerUser issuerUser) {
                        if (issuerUser.getNationalCode().trim().equals(loggedInUserNationalCode)) {
                            canCancel = true;
                            break;
                        }
                    }
                }
            }
        }
        if (!canCancel) {
            throw new RuntimeException("Can not cancel"); //TODO change this exception
        }
        runtimeService.deleteProcessInstance(processInstanceId, ProcessState.CANCELED.name());
        return true;
    }

    public ProcessResponse createResponse(ProcessInstance processInstance) {
        ProcessResponse processResponse = ProcessResponseMapper.INSTANCE.toProcessResponse(processInstance);
        processResponse.setCreatedTime(LocalDateTime.now());
        return processResponse;
    }

    private ProcessDefinition findProcessDefinitionByProcessKey(String processDefinitionKey) {
        return processEngine
                .getRepositoryService()
                .createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey)
                .latestVersion()
                .list()
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFountProcessException("CamundaProcessService", "process definition not found with key %s ".formatted(processDefinitionKey)));
    }
}
