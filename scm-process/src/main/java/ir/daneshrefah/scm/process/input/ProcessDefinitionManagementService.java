package ir.daneshrefah.scm.process.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.bpmnModel.BpmnModelInstanceResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployRequest;
import ir.daneshrefah.scm.process.service.dto.processDefinition.ProcessDeployResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessDefinitionDeleteRequest;
import ir.daneshrefah.scm.process.service.processDefinition.ProcessDefinitionManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Service
public class ProcessDefinitionManagementService extends AbstractJavaService {

    @Autowired
    private ProcessDefinitionManagement processDefinitionManagement;

    public ProcessDefinitionManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService(operationCode = SVC_PROCESS_DEFINITION_LIST)
    public PagedResponseData<ProcessDefinitionResponse> getList(ProcessDefinitionRequest request) {
        return processDefinitionManagement.findProcessDefinitionList(request);
    }

    @JavaService(operationCode = SVC_PROCESS_DEFINITION_DEPLOY)
    public ProcessDeployResponse deployProcess(ProcessDeployRequest request) throws Exception {
        return processDefinitionManagement.deployProcess(request);
    }

    @JavaService(operationCode = SVC_PROCESS_DEFINITION_GET)
    public BpmnModelInstanceResponse getProcessInstanceXml(BpmnModelInstanceRequest request) {
        return processDefinitionManagement.getProcessInstanceXml(request);
    }

    @JavaService(operationCode = SVC_PROCESS_DEFINITION_DELETE)
    public boolean deleteDefinition(ProcessDefinitionDeleteRequest request) {
        return processDefinitionManagement.deleteDefinition(request);
    }
}
