package ir.daneshrefah.scm.process.service.processDefinition;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.process.exception.definition.ProcessDefinitionExistsException;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessDefinitionDeleteRequest;
import ir.daneshrefah.scm.process.service.util.bpmnModelInstance.BpmnModelInstanceService;
import ir.daneshrefah.scm.process.service.util.processDefinition.ProcessDefinitionService;
import ir.daneshrefah.scm.process.service.util.processInstance.ProcessInstanceService;
import ir.daneshrefah.scm.utils.base64.Base64Utils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CamundaProcessDefinitionService implements ProcessDefinitionManagement {

    private final ProcessDefinitionService processDefinitionService;

    private final ProcessInstanceService processInstanceService;

    private final BpmnModelInstanceService bpmnModelInstanceService;

    @Override
    public PagedResponseData<ProcessDefinitionResponse> findProcessDefinitionList(ProcessDefinitionRequest request) {
        return processDefinitionService.getList(request);
    }

    public ProcessDeployResponse deployProcess(ProcessDeployRequest request) {
        ValidationUtils.checkEmptyString(request.getXmlBPMN(), () -> {
            throw new InvalidInputException("xmlBPMN");
        });
        ValidationUtils.checkEmptyString(request.getDeploymentName(), () -> {
            throw new InvalidInputException("DeploymentName");
        });
        Deployment deployment = processDefinitionService.deployProcess(request);
        return new ProcessDeployResponse(deployment.getId(), deployment.getName(), deployment.getDeploymentTime());
    }

    @Override
    public BpmnModelInstanceResponse getProcessInstanceXml(BpmnModelInstanceRequest request) {
        ProcessDefinition processDefinition = processDefinitionService.findByDeploymentId(request.getDeploymentId());

        if (processDefinition == null) {
            throw new InvalidInputException("No process definitions found for deployment id " + request.getDeploymentId()); //TODO Custom exception
        }

        BpmnModelInstance bpmnModelInstance = bpmnModelInstanceService.getBpmnModelInstance(processDefinition.getId());

        if (bpmnModelInstance == null) {
            throw new IllegalArgumentException("No BPMN model instance found for process definition id " + processDefinition.getId());//TODO Custom exception
        }
        BpmnModelInstanceResponse bpmnModelInstanceResponse = new BpmnModelInstanceResponse();
        String processXml = Base64Utils.encodeWithBase64(StringUtils.normalizeSpace(Bpmn.convertToString(bpmnModelInstance)));
        bpmnModelInstanceResponse.setDefinition(processXml);
        return bpmnModelInstanceResponse;
    }

    @Override
    public boolean deleteDefinition(ProcessDefinitionDeleteRequest request) {
        Long count = processInstanceService.activeCount(request.getDeploymentId());
        if (count != null && count > 0) {
            throw new ProcessDefinitionExistsException(request.getDeploymentId(), "Cannot delete deployment because process definitions exist for deployment id = :deploymentId  " + request.getDeploymentId(), request.getDeploymentId());
        }
        return processDefinitionService.deleteDefinition(request);
    }
}
