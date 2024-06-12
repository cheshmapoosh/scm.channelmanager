package ir.daneshrefah.scm.process.service.processInstance;

import ir.daneshrefah.scm.process.model.definition.ProcessDefinitionResponse;
import ir.daneshrefah.scm.process.model.response.ProcessInstanceResponse;
import ir.daneshrefah.scm.process.service.definition.DefinitionManagementService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessInstanceService implements ProcessInstanceManagement {

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private DefinitionManagementService definitionManagementService;

    @Autowired
    private HistoryService historyService;

    @Override
    public List<ProcessInstanceResponse> processInstanceList() {
        List<ProcessDefinitionResponse> processDefinitionResponseList = definitionManagementService.getList();
        List<ProcessInstanceResponse> processInstanceResponseList = new ArrayList<>();
        for (ProcessDefinitionResponse processDefinitionResponse : processDefinitionResponseList) {
            Long count = runtimeService.createProcessInstanceQuery()
                    .processDefinitionId(processDefinitionResponse.getId())
                    .active().count();
            ProcessInstanceResponse processInstanceResponse = new ProcessInstanceResponse();//TODO use mapstruct
            processInstanceResponse.setActiveCount(count);
            processInstanceResponse.setId(processDefinitionResponse.getId());
            processInstanceResponse.setKey(processDefinitionResponse.getKey());
            processInstanceResponse.setCategory(processDefinitionResponse.getCategory());
            processInstanceResponse.setDescription(processDefinitionResponse.getDescription());
            processInstanceResponse.setName(processDefinitionResponse.getName());
            processInstanceResponse.setVersion(processDefinitionResponse.getVersion());
            processInstanceResponse.setDeploymentId(processDefinitionResponse.getDeploymentId());
            processInstanceResponse.setSuspended(processDefinitionResponse.isSuspended());
            processInstanceResponse.setVersionTag(processDefinitionResponse.getVersionTag());
            processInstanceResponse.setHistoryTimeToLive(processDefinitionResponse.getHistoryTimeToLive());
            processInstanceResponseList.add(processInstanceResponse);
        }
        return processInstanceResponseList;
    }

    @Override
    public List<ProcessInstanceResponse> processInstanceDetailList(String deploymentId) {
        List<ProcessInstance> processInstanceList = runtimeService.createProcessInstanceQuery()
                .deploymentId(deploymentId)
                .active()
                .list();
        List<ProcessInstanceResponse> processInstanceResponseList = new ArrayList<>();
        for (ProcessInstance processInstance : processInstanceList) {
            ProcessInstanceResponse processInstanceResponse = new ProcessInstanceResponse();//TODO use mapstruct
            processInstanceResponse.setId(processInstance.getId());
            processInstanceResponse.setKey(processInstance.getBusinessKey());
            processInstanceResponse.setSuspended(processInstance.isSuspended());
            processInstanceResponseList.add(processInstanceResponse);
        }
        return processInstanceResponseList;
    }
}
