package ir.daneshrefah.scm.process.service.util.processDefinition;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.exception.processInstance.ProcessInstanceNotFoundWithKeyException;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessDefinitionDeleteRequest;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.repository.ProcessDefinition;

import java.util.List;

public interface ProcessDefinitionService {
    ProcessDefinition findByKey(String processKey) throws ProcessInstanceNotFoundWithKeyException;

    ProcessDefinition findById(String processDefinitionId);

    List<ProcessDefinition> getList();

    PagedResponseData<ProcessDefinitionResponse> getList(ProcessDefinitionRequest request);

    Deployment deployProcess(ProcessDeployRequest request);

    ProcessDefinition findByDeploymentId(String deploymentId);

    List<ProcessDefinition> getListByDeploymentId(String deploymentId);

    boolean deleteDefinition(ProcessDefinitionDeleteRequest request);
}
