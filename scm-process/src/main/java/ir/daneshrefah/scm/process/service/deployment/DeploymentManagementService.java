//package ir.daneshrefah.scm.process.service.deployment;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
//import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
//import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
//import ir.daneshrefah.scm.process.model.deployment.DeploymentResponse;
//import ir.daneshrefah.scm.process.model.deployment.DeployRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.FileNotFoundException;
//
//@Service
//public class DeploymentManagementService extends AbstractJavaService {
//
//    @Autowired
//    private CamundaDeploymentService camundaDeploymentService;
//
//    public DeploymentManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
//        super(producerTemplate, objectMapper);
//    }
//    @JavaService
//    public DeploymentResponse deployProcess(DeployRequest deployRequest) throws FileNotFoundException {
//        return camundaDeploymentService.deployByProcessKey(deployRequest);
//    }
//
//}
