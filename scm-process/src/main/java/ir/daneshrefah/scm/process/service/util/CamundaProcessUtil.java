package ir.daneshrefah.scm.process.service.util;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.Query;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class CamundaProcessUtil {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ProcessEngine processEngine;

    @Autowired
    private RepositoryService repositoryService;

    //TODO Refactor this method
    public Map<String, String> getExtensionProperties(Task task) {
        Map<String, String> extensionProperties = new HashMap<>();

        // Get the process definition ID
        String processDefinitionId = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult().getProcessDefinitionId();

        // Get the BPMN model instance
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinitionId);

        // Get the user task element
        UserTask userTask = modelInstance.getModelElementById(task.getTaskDefinitionKey());

        // Get the extension elements
        ExtensionElements extensionElements = userTask.getExtensionElements();
        if (extensionElements != null) {
            CamundaProperties camundaProperties = null;
            Query<ModelElementInstance> elementsQuery = extensionElements.getElementsQuery();
            if (elementsQuery.list() != null && !elementsQuery.list().isEmpty()) {
                camundaProperties = elementsQuery.filterByType(CamundaProperties.class).singleResult();
            }
            if (camundaProperties != null) {
                for (CamundaProperty property : camundaProperties.getCamundaProperties()) {
                    extensionProperties.put(property.getCamundaName(), property.getCamundaValue());
                }
            }
        }
        return extensionProperties;
    }

    //TODO Refactor this method
    public <T extends BaseElement> Map<String, String> getExtensionProperties(ProcessDefinition processDefinition, Class<T> clazz) {
        Map<String, String> extensionProperties = new HashMap<>();
        // Get the BPMN model instance
        BpmnModelInstance modelInstance = repositoryService.getBpmnModelInstance(processDefinition.getId());
        // Retrieve the model element  list
        Collection<T> modelElements = modelInstance.getModelElementsByType(clazz);
        for (T modelElement : modelElements) {
            // Get extension elements
            ExtensionElements extensionElements = modelElement.getExtensionElements();
            if (extensionElements != null) {
                CamundaProperties camundaProperties = extensionElements.getElementsQuery()
                        .filterByType(CamundaProperties.class)
                        .singleResult();
                if (camundaProperties != null) {
                    Collection<CamundaProperty> properties = camundaProperties.getCamundaProperties();
                    for (CamundaProperty property : properties) {
                        extensionProperties.put(property.getCamundaName(), property.getCamundaValue());
                    }
                }
            }
        }
        return extensionProperties;
    }

    public ProcessInstance getProcessInstance(String processInstanceId){//TODO move this method to camunda util
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    public ProcessDefinition getProcessDefinition(String processDefinitionId){//TODO move this method to camunda util
        return processEngine.getRepositoryService().createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
    }

}
