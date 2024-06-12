package ir.daneshrefah.scm.process.service.processInstance;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.model.response.ProcessInstanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProcessInstanceManagementService extends AbstractJavaService {

    @Autowired
    private ProcessInstanceManagement processInstanceManagement;

    public ProcessInstanceManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    public List<ProcessInstanceResponse> findProcessInstances(){
        return processInstanceManagement.processInstanceList();
    }

    public List<ProcessInstanceResponse> findProcessInstancesDetailList(String deploymentId){
        return processInstanceManagement.processInstanceDetailList(deploymentId);
    }
}
