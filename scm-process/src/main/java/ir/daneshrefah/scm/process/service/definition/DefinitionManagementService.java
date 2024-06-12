package ir.daneshrefah.scm.process.service.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;

import ir.daneshrefah.scm.process.model.definition.ProcessDefinitionResponse;
import org.camunda.bpm.engine.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
//TODO create camunda Definition service
public class DefinitionManagementService extends AbstractJavaService {

    @Autowired
    private RepositoryService repositoryService;

    public DefinitionManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    public List<ProcessDefinitionResponse> getList() {
        return repositoryService
                .createProcessDefinitionQuery()
                .list()
                .stream().
                map(processDefinition -> new ProcessDefinitionResponse(
                        processDefinition.getId(),
                        processDefinition.getKey(),
                        processDefinition.getCategory(),
                        processDefinition.getDescription(),
                        processDefinition.getName(),
                        processDefinition.getVersion(),
                        processDefinition.getDeploymentId(),
                        processDefinition.isSuspended(),
                        processDefinition.getVersionTag(),
                        processDefinition.getHistoryTimeToLive()
                        )).toList();
    }
}
