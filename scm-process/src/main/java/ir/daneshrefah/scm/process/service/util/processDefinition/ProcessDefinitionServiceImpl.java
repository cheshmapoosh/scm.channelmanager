package ir.daneshrefah.scm.process.service.util.processDefinition;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.exception.definition.ProcessDefinitionExistsException;
import ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundWithKeyException;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessDefinitionDeleteRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;

@Service
@AllArgsConstructor
public class ProcessDefinitionServiceImpl implements ProcessDefinitionService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;

    @Override
    public ProcessDefinition findByKey(String processKey) throws ProcessInstanceNotFoundWithKeyException {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .list()
                .stream()
                .findFirst().orElseThrow(() ->
                        new ProcessInstanceNotFoundWithKeyException("processKey", "No process instance found with key = " + processKey, processKey));
    }

    public ProcessDefinition findById(String processDefinitionId) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
    }

    public ProcessDefinition findByDeploymentId(String deploymentId) {
        return repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).singleResult();
    }

    public List<ProcessDefinition> getListByDeploymentId(String deploymentId) {
        return repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).list();
    }

    public List<ProcessDefinition> getList() {
        return repositoryService
                .createProcessDefinitionQuery()
                .list();
    }

    public PagedResponseData<ProcessDefinitionResponse> getList(ProcessDefinitionRequest request) {
        ProcessDefinitionQuery processDefinitionQuery =repositoryService.createProcessDefinitionQuery().latestVersion();
        int firstResult = (request.getPageNo() - 1) * request.getPageSize();
        int maxResult = Math.max((request.getPageNo() * request.getPageSize()) - 1, request.getPageSize());
        long count = processDefinitionQuery.count();
        List<ProcessDefinitionResponse> processDefinitionResponses = processDefinitionQuery
                .listPage(firstResult, maxResult)
                .stream().map(processDefinition -> {
                    ProcessDefinitionResponse processDefinitionResponse = new ProcessDefinitionResponse();
                    processDefinitionResponse.setId(processDefinition.getId());
                    processDefinitionResponse.setKey(processDefinition.getKey());
                    processDefinitionResponse.setCategory(processDefinition.getCategory());
                    processDefinitionResponse.setDescription(processDefinition.getDescription());
                    processDefinitionResponse.setName(processDefinition.getName());
                    processDefinitionResponse.setVersion(processDefinition.getVersion());
                    processDefinitionResponse.setDeploymentId(processDefinition.getDeploymentId());
                    processDefinitionResponse.setSuspended(processDefinition.isSuspended());
                    processDefinitionResponse.setVersionTag(processDefinition.getVersionTag());
                    processDefinitionResponse.setHistoryTimeToLive(processDefinition.getHistoryTimeToLive());
                    if (request.isIncludeActiveCount()) {
                        long activeProcessCount = runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinition.getKey()).active().count();
                        processDefinitionResponse.setActiveCount(activeProcessCount);
                    }
                    return processDefinitionResponse;
                }).toList();
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), count, processDefinitionResponses);
    }

    @Override
    public Deployment deployProcess(ProcessDeployRequest request) {
        byte[] decodedBytes = Base64.getDecoder().decode(request.getXmlBPMN());
        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(new ByteArrayInputStream(decodedBytes));
        return repositoryService.createDeployment()
                .addModelInstance("%s.bpmn".formatted(request.getDeploymentName()), modelInstance)
                .name(request.getDeploymentName())
                .deploy();
    }

    @Override
    public boolean deleteDefinition(ProcessDefinitionDeleteRequest request) {
        repositoryService.deleteDeployment(request.getDeploymentId(), false, true, true);
        return true;
    }
}