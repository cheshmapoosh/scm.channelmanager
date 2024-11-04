package ir.daneshrefah.scm.process.service.util.processInstance;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.process.service.constant.ProcessConstants;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import lombok.AllArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ProcessInstanceServiceImpl implements ProcessInstanceService {

    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final ProcessEngine processEngine;

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
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery().processDefinitionId(request.getDefinitionId()).unfinished();
        long count = historicProcessInstanceQuery.count();
        CommandExecutor commandExecutor = ((ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration()).getCommandExecutorTxRequired();
        List<ProcessInstanceResponse> processInstances = historicProcessInstanceQuery.listPage(firstResult, maxResult).stream().map(historicProcessInstance -> {
            ProcessInstanceResponse processInstanceResponse = new ProcessInstanceResponse();
            processInstanceResponse.setId(historicProcessInstance.getId());
            processInstanceResponse.setKey(historicProcessInstance.getBusinessKey());
            IssuerInfo issuerInfo = (IssuerInfo) commandExecutor.execute(commandContext -> runtimeService.getVariable(historicProcessInstance.getId(), ProcessConstants.ISSUER_INFO));
            ProcessInstance processInstance = commandExecutor.execute(commandContext -> runtimeService.createProcessInstanceQuery().processInstanceId(historicProcessInstance.getId()).singleResult());
            processInstanceResponse.setSuspended(processInstance.isSuspended());
            processInstanceResponse.setUsername(issuerInfo.getPersonUsername());
            processInstanceResponse.setStartTime(historicProcessInstance.getStartTime());
            return processInstanceResponse;
        }).toList();
        return new PagedResponseData<>(request.getPageNo(), request.getPageSize(), count, processInstances);
    }

    public Long activeCount(String definitionId) {
        return runtimeService.createProcessInstanceQuery().processDefinitionId(definitionId).active().count();
    }
}