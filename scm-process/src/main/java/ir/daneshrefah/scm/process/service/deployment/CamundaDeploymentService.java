//package ir.daneshrefah.scm.process.service.deployment;
//
//import ir.daneshrefah.scm.process.exception.DeployDefinitionException;
//import ir.daneshrefah.scm.process.exception.NotFountProcessException;
//import ir.daneshrefah.scm.process.model.constant.ProcessDeploymentEnum;
//import ir.daneshrefah.scm.process.model.deployment.DeployRequest;
//import ir.daneshrefah.scm.process.model.deployment.DeploymentResponse;
//import org.camunda.bpm.engine.ProcessEngine;
//import org.camunda.bpm.engine.RepositoryService;
//import org.camunda.bpm.engine.repository.Deployment;
//import org.camunda.bpm.engine.repository.ProcessDefinition;
//import org.camunda.bpm.model.bpmn.Bpmn;
//import org.camunda.bpm.model.bpmn.BpmnModelInstance;
//import org.camunda.bpm.model.bpmn.instance.Process;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.FileSystemResourceLoader;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//
//@Component
////TODO this implementation will change,don't read from file
//public class CamundaDeploymentService {
//    private static final String DEPLOYMENT_HANDLER = "DeploymentHandler";
//
//    @Autowired
//    private ProcessEngine processEngine;
//
//    @Autowired
//    private RepositoryService repositoryService;
//
//    private Deployment loadAndDeployProcessDefinition(ProcessDeploymentEnum processDeploymentEnum) throws FileNotFoundException {
//        FileInputStream fis = getFileInputStream(processDeploymentEnum);
//        return deployProcess(processDeploymentEnum, fis);
//    }
//
//
//    private Deployment deployProcess(ProcessDeploymentEnum processDeploymentEnum, FileInputStream fis) {
//        return processEngine
//                .getRepositoryService()
//                .createDeployment()
//                .addInputStream(processDeploymentEnum.getFileName(), fis)
//                .deploy();
//    }
//
//    public void ensureProcessDefinitionIsDeployed(String processDefinitionKey) {
//        repositoryService.createProcessDefinitionQuery()
//                .processDefinitionKey(processDefinitionKey)
//                .latestVersion()
//                .list()
//                .stream()
//                .findFirst().orElseThrow(() -> new NotFountProcessException(DEPLOYMENT_HANDLER, "process definition not found with key %s ".formatted(processDefinitionKey)));
//    }
//
////    public DeploymentResponse deployByProcessKey(DeployRequest deployRequest) throws FileNotFoundException {
////        ProcessDeploymentEnum processDeploymentEnum = ProcessDeploymentEnum.findByKeyName(deployRequest.getDeploymentKey());//TODO change this implemention,don't read from file
////        if (areProcessDefinitionVersionsEqual(processDeploymentEnum)) {
////            throw new DeployDefinitionException(DEPLOYMENT_HANDLER, "The process definition versions are identical.");
////        }
////        Deployment deployment = loadAndDeployProcessDefinition(processDeploymentEnum);
////        DeploymentResponse deploymentResponse = new DeploymentResponse();
////        deploymentResponse.setId(deployment.getId());
////        deploymentResponse.setName(deployment.getName());
////        deploymentResponse.setDeploymentTime(deployment.getDeploymentTime());
////        deploymentResponse.setSource(deployment.getSource());
////        deploymentResponse.setTenantId(deployment.getTenantId());
////        return deploymentResponse;
////    }
//
//    private boolean areProcessDefinitionVersionsEqual(ProcessDeploymentEnum processDeploymentEnum) throws FileNotFoundException {
//        ProcessDefinition processDefinition = getProcessDefinitionByProcessKey(processDeploymentEnum);
//        if (processDefinition == null) {
//            return false;
//        }
//
//        getBpmnVersion(fileInputStream);
//        String deployedVersionTag = processDefinition.getVersionTag();
//        return bpmnVersion.equalsIgnoreCase(deployedVersionTag);
//    }
//
//    private ProcessDefinition getProcessDefinitionByProcessKey(ProcessDeploymentEnum processDeploymentEnum) {
//        return processEngine
//                .getRepositoryService()
//                .createProcessDefinitionQuery()
//                .processDefinitionKey()
//                .latestVersion()
//                .singleResult();
//    }
//
//    private String getBpmnVersion(InputStream bpmnInputStream) {
//        BpmnModelInstance modelInstance = Bpmn.readModelFromStream(bpmnInputStream);
//        Process process = modelInstance.getModelElementsByType(Process.class).iterator().next();
//        return process.getCamundaVersionTag();
//    }
//}
