package ir.daneshrefah.scm.process.service.util.processInstance;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import org.camunda.bpm.engine.runtime.ProcessInstance;

import java.util.Map;

public interface ProcessInstanceService {
    ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> businessData);

    ProcessInstance startProcessInstanceById(String processDefinitionId);

    void deleteProcessInstanceById(String processInstance);

    ProcessInstance getProcessInstance(String processInstance);

    Long activeCount(String deploymentId);

    PagedResponseData<ProcessInstanceResponse> getActiveProcessInstance(ProcessInstanceRequest request);
}
