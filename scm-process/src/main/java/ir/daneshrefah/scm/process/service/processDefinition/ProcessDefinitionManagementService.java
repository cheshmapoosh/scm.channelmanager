package ir.daneshrefah.scm.process.service.processDefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessDefinitionManagementService extends AbstractJavaService {

    @Autowired
    private ProcessDefinitionManagement processDefinitionManagement;

    public ProcessDefinitionManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService
    public PagedResponseData<ProcessDefinitionResponse> getList(ProcessDefinitionRequest request) {
        return processDefinitionManagement.getList(request);
    }

    @JavaService
    public ProcessDeployResponse deployProcess(ProcessDeployRequest request) throws Exception {
        return processDefinitionManagement.deployProcess(request);
    }

    @JavaService
    public BpmnModelInstanceResponse getProcessInstanceXml(BpmnModelInstanceRequest request) {
        return processDefinitionManagement.getProcessInstanceXml(request);
    }

    @JavaService
    public boolean deleteDefinition(ProcessInstanceRequest request) {
        return processDefinitionManagement.deleteDefinition(request);
    }
}
