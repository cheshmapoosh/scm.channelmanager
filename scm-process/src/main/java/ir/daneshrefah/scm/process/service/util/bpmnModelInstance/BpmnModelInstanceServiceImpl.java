package ir.daneshrefah.scm.process.service.util.bpmnModelInstance;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BpmnModelInstanceServiceImpl implements BpmnModelInstanceService {
    private final RepositoryService repositoryService;

    @Override
    public BpmnModelInstance getBpmnModelInstance(ProcessDefinition processDefinition) {
        return repositoryService.getBpmnModelInstance(processDefinition.getId());
    }

    @Override
    public BpmnModelInstance getBpmnModelInstance(String processDefinitionId) {
        return repositoryService.getBpmnModelInstance(processDefinitionId);
    }
}
