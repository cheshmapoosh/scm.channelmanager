package ir.daneshrefah.scm.process.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.process.ProcessCancelRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import ir.daneshrefah.scm.process.service.process.ProcessManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Service
public class ProcessManagementService extends AbstractJavaService {

    @Autowired
    private ProcessManagement processManagement;

    public ProcessManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper) {
        super(producerTemplate, objectMapper);
    }

    @JavaService(operationCode = SVC_PROCESS_START)
    public ProcessStartResponse startProcess(ProcessStartRequest processStartRequest) throws Exception {
        return processManagement.startProcess(processStartRequest);
    }

    @JavaService(operationCode = SVC_PROCESS_CANCEL)
    public boolean cancelProcess(ProcessCancelRequest processCancelRequest) throws Exception {
        return processManagement.cancelProcess(processCancelRequest);
    }

    @JavaService(operationCode = SVC_PROCESS_ACTIVE_LIST)
    public PagedResponseData<ProcessInstanceResponse> getActiveProcess(ProcessInstanceRequest request) {
        return processManagement.getActiveProcess(request);
    }

    @JavaService(operationCode = SVC_PROCESS_ACTIVE_COUNT)
    public Long activeCount(ProcessInstanceRequest request) {
        return processManagement.activeCount(request);
    }
}
