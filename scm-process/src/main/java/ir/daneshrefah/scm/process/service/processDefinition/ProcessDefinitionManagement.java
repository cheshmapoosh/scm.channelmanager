package ir.daneshrefah.scm.process.service.processDefinition;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessDefinitionDeleteRequest;

public interface ProcessDefinitionManagement {

    PagedResponseData<ProcessDefinitionResponse> findProcessDefinitionList(ProcessDefinitionRequest request);

    ProcessDeployResponse deployProcess(ProcessDeployRequest request) throws Exception;

    BpmnModelInstanceResponse getProcessInstanceXml(BpmnModelInstanceRequest request);

    boolean deleteDefinition(ProcessDefinitionDeleteRequest request);
}
