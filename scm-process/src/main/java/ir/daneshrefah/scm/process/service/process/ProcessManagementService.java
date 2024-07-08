package ir.daneshrefah.scm.process.service.process;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.process.ProcessCancelRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProcessManagementService extends AbstractJavaService {

    @Autowired
    private ProcessManagement processManagement;

    public ProcessManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService
    public ProcessStartResponse startProcess(ProcessStartRequest processStartRequest) throws Exception {
        return processManagement.startProcess(processStartRequest);
    }

    @JavaService
    public boolean cancelProcess(ProcessCancelRequest processCancelRequest) throws Exception {
        return processManagement.cancelProcess(processCancelRequest);
    }
}
