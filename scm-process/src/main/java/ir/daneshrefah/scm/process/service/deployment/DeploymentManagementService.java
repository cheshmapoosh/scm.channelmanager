package ir.daneshrefah.scm.process.service.deployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.config.DeploymentHandler;
import ir.daneshrefah.scm.process.model.deployment.DeploymentResponse;
import ir.daneshrefah.scm.process.model.deployment.DeployRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;

@Service
public class DeploymentManagementService extends AbstractJavaService {

    @Autowired
    private DeploymentHandler deploymentHandler;

    public DeploymentManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    public DeploymentResponse deployProcess(DeployRequest deployRequest) throws FileNotFoundException {
        return deploymentHandler.deployByProcessKey(deployRequest);
    }
}
