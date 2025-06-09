package ir.daneshrefah.scm.task.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Slf4j
@Service
public class ProcessInstanceService extends AbstractJavaService {

    private final ProcessManagementService processManagementService;

    public ProcessInstanceService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ProcessManagementService processManagementService) {
        super(producerTemplate, objectMapper);
        this.processManagementService = processManagementService;
    }

    @JavaService(operationCode = SVC_CARTABLE_START_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceStartResponse start(ProcessInstanceStartRequest processInstanceStartRequest) {
        try {
            return processManagementService.start(processInstanceStartRequest);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_GET_ALL_PROCESS)
    @SuppressWarnings("unused")
    public PagedResponseData<ProcessInstanceResponse> findAll(ProcessInstanceFilterRequest processInstanceFilterRequest) {
        try {
            return processManagementService.findAll(processInstanceFilterRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION)
    @SuppressWarnings("unused")
    public ProcessInstanceUpdateResponse updateDescription(ProcessInstanceUpdateRequest request) {
        try {
            return processManagementService.updateDescription(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_CANCEL_PROCESS)
    @SuppressWarnings("unused")
    public void cancelProcess(ProcessInstanceCancelRequest request) {
        try {
            processManagementService.cancelProcess(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_COMPLETE_PROCESS)
    @SuppressWarnings("unused")
    public void complete(ProcessInstanceCompleteRequest request) {
        try {
            processManagementService.complete(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_APPROVE_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceApproveResponse approve(ProcessInstanceApproveRequest request) {
        try {
            return processManagementService.approve(request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
