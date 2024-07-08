package ir.daneshrefah.scm.process.service.processInstance;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import org.springframework.stereotype.Service;

@Service
public class ProcessInstanceManagementService extends AbstractJavaService {

    private final ProcessInstanceManagement ProcessInstanceManagement;

    public ProcessInstanceManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ProcessInstanceManagement processInstanceManagement) {
        super(producerTemplate, objectMapper);
        ProcessInstanceManagement = processInstanceManagement;
    }

    @JavaService
    public PagedResponseData<ProcessInstanceResponse> getList(ProcessInstanceRequest request) {
        return ProcessInstanceManagement.getList(request);
    }

    @JavaService
    public Long activeCount(ProcessInstanceRequest request){
        return ProcessInstanceManagement.activeCount(request);
    }
}
