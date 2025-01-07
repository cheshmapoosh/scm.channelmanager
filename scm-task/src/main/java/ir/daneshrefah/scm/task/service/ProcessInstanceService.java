package ir.daneshrefah.scm.task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.model.*;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;

@Service
public class ProcessInstanceService extends AbstractJavaService {

    private final ProcessManagementService processManagementService;

    public ProcessInstanceService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ProcessManagementService processManagementService) {
        super(producerTemplate, objectMapper);
        this.processManagementService = processManagementService;
    }

    @JavaService(serviceCode = SVC_CARTABLE_START_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceStartResponse start(ProcessInstanceStartRequest processInstanceStartRequest) {
        return processManagementService.start(processInstanceStartRequest);
    }

    @JavaService(serviceCode = SVC_CARTABLE_GET_ALL_PROCESS)
    @SuppressWarnings("unused")
    public PagedResponseData<ProcessInstanceResponse> findAll(ProcessInstanceFilterRequest processInstanceFilterRequest) {
        return processManagementService.findAll(processInstanceFilterRequest);
    }

    @JavaService(serviceCode = SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION)
    @SuppressWarnings("unused")
    public ProcessInstanceUpdateResponse updateDescription(ProcessInstanceUpdateRequest request) {
        return processManagementService.updateDescription(request);
    }

    @JavaService(serviceCode = SVC_CARTABLE_COMPLETE_PROCESS)
    @SuppressWarnings("unused")
    public void complete(ProcessInstanceCompleteRequest request) {
        processManagementService.complete(request);
    }

    @JavaService(serviceCode = SVC_CARTABLE_APPROVE_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceApproveResponse approve(ProcessInstanceApproveRequest request) {
        return processManagementService.approve(request);
    }
}
