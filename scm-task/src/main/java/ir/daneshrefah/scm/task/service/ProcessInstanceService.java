package ir.daneshrefah.scm.task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.model.*;
import org.springframework.stereotype.Service;

@Service
public class ProcessInstanceService extends AbstractJavaService {

    private final ProcessManagementService processManagementService;

    public ProcessInstanceService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ProcessManagementService processManagementService) {
        super(producerTemplate, objectMapper);
        this.processManagementService = processManagementService;
    }

    @JavaService
    @SuppressWarnings("unused")
    public ProcessInstanceStartResponse start(ProcessInstanceStartRequest processInstanceStartRequest) {
        return processManagementService.start(processInstanceStartRequest);
    }

    @JavaService
    @SuppressWarnings("unused")
    public PagedResponseData<ProcessInstanceResponse> findAll(ProcessInstanceFilterRequest processInstanceFilterRequest) {
        return processManagementService.findAll(processInstanceFilterRequest);
    }

    @JavaService
    @SuppressWarnings("unused")
    public ProcessInstanceUpdateResponse updateDescription(ProcessInstanceUpdateRequest request) {
        return processManagementService.updateDescription(request);
    }

    @JavaService
    @SuppressWarnings("unused")
    public void complete(ProcessInstanceCompleteRequest request) {
        processManagementService.complete(request);
    }

    @JavaService
    @SuppressWarnings("unused")
    public ProcessInstanceApproveResponse approve(ProcessInstanceApproveRequest request) {
        return processManagementService.approve(request);
    }
}
