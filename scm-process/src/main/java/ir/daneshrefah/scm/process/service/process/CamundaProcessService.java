package ir.daneshrefah.scm.process.service.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.entity.ProcessStartLog;
import ir.daneshrefah.scm.process.exception.common.UnauthorizedException;
import ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundException;
import ir.daneshrefah.scm.process.exception.schema.JsonSchemaException;
import ir.daneshrefah.scm.process.model.mapper.ProcessResponseMapper;
import ir.daneshrefah.scm.process.model.process.ProcessMetadata;
import ir.daneshrefah.scm.process.service.dto.process.ProcessCancelRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import ir.daneshrefah.scm.process.service.util.JsonTransformationUtil;
import ir.daneshrefah.scm.process.service.util.ProcessMetadataExtractor;
import ir.daneshrefah.scm.process.service.util.ValidateUserService;
import ir.daneshrefah.scm.process.service.util.ValidationSchema;
import ir.daneshrefah.scm.process.service.util.historicProcess.HistoricProcessService;
import ir.daneshrefah.scm.process.service.util.processDefinition.ProcessDefinitionService;
import ir.daneshrefah.scm.process.service.util.processInstance.ProcessInstanceService;
import ir.daneshrefah.scm.process.service.util.processVariable.ProcessVariableService;
import ir.daneshrefah.scm.service.ProcessStartLogService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.model.bpmn.instance.StartEvent;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
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
    private final ProcessStartLogService processStartLogService;
    private final ObjectMapper objectMapper;



    public ProcessStartResponse startProcess(ProcessStartRequest processStartRequest) throws Exception {
        ProcessStartLog processStartLog = new ProcessStartLog();
        ProcessInstance processInstance;
        try {
            IssuerInfo issuerInfo = AuthenticationUtils.getIssuerInfo();
            processStartLog.setStartAt(new Date());
            processStartLog.setIssuerUserName(issuerInfo.getPersonUsername());
            processStartLog.setProcessKey(processStartRequest.getProcessKey());
            processStartLog.setPayload(objectMapper.writeValueAsString(processStartRequest));
            if (processStartRequest.getData().has(BUSINESS_DATA)) {
                throw new InvalidInputException(BUSINESS_DATA);
            }
            ProcessDefinition processDefinition = processDefinitionService.findByKey(processStartRequest.getProcessKey());

            ProcessMetadata metadata = metadataExtractor.extractProcessMetadata(processDefinition, StartEvent.class);

            validateProcessBeforeStart(metadata, processStartRequest);

            jsonTransformationUtil.transformBusinessData(processStartRequest.getData(), metadata.getInputConverters());

            Map<String, Object> data = objectMapper.convertValue(processStartRequest.getData(), new TypeReference<>() {
            });

            Map<String, Object> businessData = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                businessData.put(BUSINESS_DATA + "_" + entry.getKey(), entry.getValue());
            }

            businessData.computeIfAbsent(ISSUER_INFO, s -> issuerInfo);
            processInstance = processInstanceService.startProcessInstanceById(processDefinition.getId(), businessData);
            processStartLog.setProcessId(processInstance.getId());
            processStartLog.setStatus("ACCEPT");//TODO: move it to enum
            processStartLogService.save(processStartLog);

        } catch (Exception e) {
            processStartLog.setStatus("FAILED");
            processStartLog.setExceptionClassName(e.getClass().getName());
            throw e;
        } finally {
            processStartLogService.save(processStartLog);
        }
        return createResponse(processInstance);
    }

    private void validateProcessBeforeStart(ProcessMetadata metadata, ProcessStartRequest processStartRequest) throws JsonProcessingException {
        String loggedInUser = Objects.requireNonNull(AuthenticationUtils.getLoggedInUser()).getPerson().getNationality().getCode();
        if (Objects.nonNull(metadata.getStartValidationSchema())) {
            Set<ValidationMessage> validationMessages = ValidationSchema.validate(processStartRequest, metadata.getStartValidationSchema(), "");
            if (validationMessages != null && !validationMessages.isEmpty()) {
                String messageException = validationMessages.stream().map(ValidationMessage::getMessage).collect(Collectors.joining("\\n"));
                throw new JsonSchemaException("Input", messageException);
            }
        }
        if (StringUtils.isNotEmpty(metadata.getStartValidationScript())) {
            try (Context context = Context.create()) {
                Value js = context.getBindings("js");
                js.putMember("loggedInUser", loggedInUser);
                js.putMember("date", processStartRequest);
                context.eval("js", metadata.getStartValidationScript());
            } catch (Exception e) {
                //TODO
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
        ProcessMetadata metadata = metadataExtractor.extractProcessMetadata(processDefinition, StartEvent.class);
        return validateUserService.isUserInAuthorizedRoles(metadata.getStartAuthorizedAuthorities()) ||
                validateUserService.isUserAuthorized(variables, userName, metadata.getCancelAuthorizedUsers());
    }

    @Override
    public PagedResponseData<ProcessInstanceResponse> getActiveProcess(ProcessInstanceRequest request) {
        return processInstanceService.getActiveProcessInstance(request);
    }

    @Override
    public Long activeCount(ProcessInstanceRequest request) {
        return processInstanceService.activeCount(request.getDefinitionId());
    }
}
