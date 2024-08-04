package ir.daneshrefah.scm.process.service.util;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.process.service.util.bpmnModelInstance.BpmnModelInstanceService;
import ir.daneshrefah.scm.process.service.util.historicProcess.HistoricProcessService;
import ir.daneshrefah.scm.process.service.util.processDefinition.ProcessDefinitionService;
import ir.daneshrefah.scm.process.service.util.processInstance.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.ServiceTask;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class BpmnExtensionExtractor {
    private final HistoricProcessService historicProcessService;
    private final BpmnModelInstanceService bpmnModelInstanceService;
    private final ProcessDefinitionService processDefinitionService;
    private final ProcessInstanceService processInstanceService;
    private final CacheTemplate cacheTemplate;

    public <T extends ModelElementInstance> Map<String, String> getExtensionProperties(String processInstanceId, Class<T> clazz) {
        HistoricProcessInstance historicProcessInstance = historicProcessService.getProcessInstance(processInstanceId);
        ProcessDefinition processDefinition = processDefinitionService.findById(historicProcessInstance.getProcessDefinitionId());
        return getExtensionProperties(processDefinition, clazz);
    }

    public <T extends ModelElementInstance> Map<String, String> getExtensionProperties(ProcessDefinition processDefinition, Class<T> clazz) {
        BpmnModelInstance modelInstance = bpmnModelInstanceService.getBpmnModelInstance(processDefinition.getId());
        return getExtensionProperties(modelInstance, clazz);
    }

    public <T extends ModelElementInstance> Map<String, String> getExtensionProperties(BpmnModelInstance modelInstance, Class<T> clazz) {
        Map<String, String> extensionProperties = new HashMap<>();
        Collection<T> modelElements = modelInstance.getModelElementsByType(clazz);
        for (T modelElement : modelElements) {
            if (modelElement instanceof BaseElement baseElement) {
                ExtensionElements extensionElements = baseElement.getExtensionElements();
                extractExtension(extensionProperties, extensionElements);
            }
        }
        return extensionProperties;
    }

    public Map<String, String> getExtensionProperties(Task task) {
        Map<String, String> extensionProperties = (Map<String, String>) cacheTemplate.getFromCache(task.getProcessDefinitionId(),task.getTaskDefinitionKey());
        if (extensionProperties != null) {
            return extensionProperties;
        }
        extensionProperties = new HashMap<>();
        ProcessInstance processInstance = processInstanceService.getProcessInstance(task.getProcessInstanceId());
        BpmnModelInstance modelInstance = bpmnModelInstanceService.getBpmnModelInstance(processInstance.getProcessDefinitionId());
        BaseElement baseElement = modelInstance.getModelElementById(task.getTaskDefinitionKey());
        if (baseElement != null) {
            if (baseElement instanceof UserTask) {
                extractExtension(extensionProperties,baseElement.getExtensionElements());
            } else if (baseElement instanceof ServiceTask) {
                extractExtension(extensionProperties,baseElement.getExtensionElements());
            }
        }
        cacheTemplate.putInCache(task.getProcessDefinitionId(), task.getName(), extensionProperties);
        return extensionProperties;
    }

    private static void extractExtension(Map<String, String> extensionProperties, ExtensionElements extensionElements) {
        if (extensionElements != null) {
            CamundaProperties camundaProperties = extensionElements.getElementsQuery().filterByType(CamundaProperties.class).singleResult();
            if (camundaProperties != null) {
                Collection<CamundaProperty> properties = camundaProperties.getCamundaProperties();
                for (CamundaProperty property : properties) {
                    extensionProperties.put(property.getCamundaName(), property.getCamundaValue());
                }
            }
        }
    }
}
