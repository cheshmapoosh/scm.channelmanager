package ir.daneshrefah.scm.process.service.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.process.exception.common.UnauthorizedException;
import ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundException;
import ir.daneshrefah.scm.process.exception.schema.JsonSchemaException;
import ir.daneshrefah.scm.process.model.mapper.ProcessResponseMapper;
import ir.daneshrefah.scm.process.model.process.ProcessMetadata;
import ir.daneshrefah.scm.process.service.dto.process.ProcessCancelRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;
import ir.daneshrefah.scm.process.service.util.JsonTransformationUtil;
import ir.daneshrefah.scm.process.service.util.ProcessMetadataExtractor;
import ir.daneshrefah.scm.process.service.util.ValidateUserService;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
import ir.daneshrefah.scm.process.service.util.historicProcess.HistoricProcessService;
import ir.daneshrefah.scm.process.service.util.processDefinition.ProcessDefinitionService;
import ir.daneshrefah.scm.process.service.util.processInstance.ProcessInstanceService;
import ir.daneshrefah.scm.process.service.util.processVariable.ProcessVariableService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.BUSINESS_DATA;
import static ir.daneshrefah.scm.process.service.constant.ProcessConstants.ISSUER_INFO;

@Component
@RequiredArgsConstructor
public class CamundaProcessService implements ProcessManagement {
    private final ProcessDefinitionService processDefinitionService;
    private final ProcessInstanceService processInstanceService;
    private final ProcessVariableService processVariableService;
    private final ProcessMetadataExtractor metadataExtractor;
    private final HistoricProcessService historicProcessService;
    private final JsonTransformationUtil jsonTransformationUtil;
    private final ValidateUserService validateUserService;

    public ProcessStartResponse startProcess(ProcessStartRequest processStartRequest) throws Exception {
        ProcessDefinition processDefinition = processDefinitionService.findByKey(processStartRequest.getProcessKey());
        //TODO use cache
        ProcessMetadata metadata = metadataExtractor.extractProcessMetadata(processDefinition, StartEvent.class);

        validateProcessBeforeStart(metadata, processStartRequest);

        jsonTransformationUtil.transformBusinessData(processStartRequest.getData(), metadata.getInputConverters());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> data = objectMapper.convertValue(processStartRequest.getData(), new TypeReference<>() {
        });

        Map<String, Object> businessData = new HashMap<>();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            businessData.put(BUSINESS_DATA + "_" + entry.getKey(), entry.getValue());
        }

        IssuerInfo issuerInfo = AuthenticationUtils.getIssuerInfo();
        businessData.computeIfAbsent(ISSUER_INFO, s -> issuerInfo);
        ProcessInstance processInstance = processInstanceService.startProcessInstanceById(processDefinition.getId(), businessData);
        return createResponse(processInstance);
    }

    private void validateProcessBeforeStart(ProcessMetadata metadata, ProcessStartRequest processStartRequest) throws JsonProcessingException {
        String loggedInUser = AuthenticationUtils.getLoggedInUser().getPerson().getNationality().getCode();
        if (StringUtils.isNotEmpty(metadata.getStartValidationSchema())) {
            Set<ValidationMessage> validationMessages = ValidationSchema.validate(processStartRequest, metadata.getStartValidationSchema(), "");
            if (validationMessages != null && !validationMessages.isEmpty()) {
                String messageException = validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\\n"));
                throw new JsonSchemaException("Input", String.join("\n", messageException));
            }
        }
    }

    public ProcessStartResponse createResponse(ProcessInstance processInstance) {
        ProcessStartResponse processStartResponse = ProcessResponseMapper.INSTANCE.toProcessResponse(processInstance);
        processStartResponse.setCreatedTime(LocalDateTime.now());
        return processStartResponse;
    }

    public boolean cancelProcess(ProcessCancelRequest processCancelRequest) throws Exception {
        String processInstanceId = processCancelRequest.getProcessId();
        HistoricProcessInstance historicProcessInstance = historicProcessService.getActiveProcessInstance(processInstanceId);
        if (historicProcessInstance == null) {
            throw new ProcessInstanceNotFoundException("processInstanceId", "No process instance found with ID = " + processInstanceId, processInstanceId);
        }
        Map<String, Object> variables = processVariableService.getProcessVariables(processInstanceId);
        ProcessDefinition processDefinition = processDefinitionService.findById(historicProcessInstance.getProcessDefinitionId());
        if (!isUserAuthorizedToCancelProcess(variables, processDefinition)) {
            throw new UnauthorizedException("Authorization", "Unauthorized to cancel the process");
        }
        processInstanceService.deleteProcessInstanceById(processInstanceId);
        return true;
    }

    private boolean isUserAuthorizedToCancelProcess(Map<String, Object> variables, ProcessDefinition processDefinition) throws Exception {
        GeneralPerson generalPerson = Objects.requireNonNull(AuthenticationUtils.getLoggedInUser()).getPerson();
        String userName = generalPerson.getUsername();
        //TODO use cache
        ProcessMetadata metadata = metadataExtractor.extractProcessMetadata(processDefinition, StartEvent.class);
        return validateUserService.isUserInAuthorizedRoles(metadata.getStartAuthorizedAuthorities()) ||
                validateUserService.isUserAuthorized(variables, userName, metadata.getCancelAuthorizedUsers());
    }
}
