package ir.daneshrefah.scm.process.service.util.bpmnModelInstance;

import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public interface BpmnModelInstanceService {
    BpmnModelInstance getBpmnModelInstance(ProcessDefinition processDefinition);
    BpmnModelInstance getBpmnModelInstance(String processDefinitionId) ;
}
