package ir.daneshrefah.scm.process.service.util.processInstance;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.service.constant.ProcessConstants;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private final RuntimeService runtimeService;

    @Override
    public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, Object> businessData) {
        return runtimeService.startProcessInstanceById(processDefinitionId, businessData);
    }

    @Override
    public ProcessInstance startProcessInstanceById(String processDefinitionId) {
        return runtimeService.startProcessInstanceByKey(processDefinitionId);
    }

    @Override
    public void deleteProcessInstanceById(String processInstanceId) {
        runtimeService.deleteProcessInstance(processInstanceId, ProcessConstants.CANCELED);
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstance) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstance).singleResult();
    }

    @Override
    public PagedResponseData<ProcessInstanceResponse> getActiveProcessInstance(ProcessInstanceRequest request) {
        int firstResult = (request.getPageNo() - 1) * request.getPageSize();
        int maxResult = Math.max((request.getPageNo() * request.getPageSize()) - 1, request.getPageSize());
        ProcessInstanceQuery processInstanceQuery = runtimeService.createProcessInstanceQuery().deploymentId(request.getDeploymentId()).active();
        long count = processInstanceQuery.count();
        List<ProcessInstanceResponse> processInstances = processInstanceQuery.listPage(firstResult, maxResult).stream().map(processInstance -> {
            ProcessInstanceResponse processInstanceResponse = new ProcessInstanceResponse();
            processInstanceResponse.setId(processInstance.getId());
            processInstanceResponse.setKey(processInstance.getBusinessKey());
            return processInstanceResponse;
        }).toList();
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), count, processInstances);
    }

    public Long activeCount(String deploymentId) {
        return runtimeService.createProcessInstanceQuery().deploymentId(deploymentId).active().count();
    }
}