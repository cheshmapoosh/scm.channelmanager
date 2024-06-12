package ir.daneshrefah.scm.process.service.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.model.request.CancelProcessRequest;
import ir.daneshrefah.scm.process.model.request.ProcessRequest;
import ir.daneshrefah.scm.process.model.response.ProcessResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessManagementService extends AbstractJavaService {

    @Autowired
    private ProcessManagement processManagement;

    public ProcessManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    public ProcessResponse startProcess(ProcessRequest processRequest) throws JsonProcessingException {
        return processManagement.startProcess(processRequest);
    }

    public boolean cancelProcess(CancelProcessRequest cancelProcessRequest) throws Exception {
        return processManagement.cancelProcess(cancelProcessRequest);
    }
}
