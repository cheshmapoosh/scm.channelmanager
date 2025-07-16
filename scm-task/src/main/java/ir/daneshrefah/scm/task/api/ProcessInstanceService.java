package ir.daneshrefah.scm.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.model.*;
import ir.daneshrefah.scm.task.service.ProcessManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
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
    public ProcessInstanceStartResponse start(Exchange exchange, @Body ProcessInstanceStartRequest processInstanceStartRequest) {
        try {
            return processManagementService.start(exchange,processInstanceStartRequest);
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_GET_ALL_PROCESS)
    @SuppressWarnings("unused")
    public PagedResponseData<ProcessInstanceResponse> findAll(Exchange exchange, @Body ProcessInstanceFilterRequest processInstanceFilterRequest) {
        try {
            return processManagementService.findAll(exchange,processInstanceFilterRequest);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION)
    @SuppressWarnings("unused")
    public ProcessInstanceUpdateResponse updateDescription(Exchange exchange, @Body ProcessInstanceUpdateRequest request) {
        try {
            return processManagementService.updateDescription(exchange,request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_CANCEL_PROCESS)
    @SuppressWarnings("unused")
    public void cancelProcess(Exchange exchange, @Body ProcessInstanceCancelRequest request) {
        try {
            processManagementService.cancelProcess(exchange,request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_COMPLETE_PROCESS)
    @SuppressWarnings("unused")
    public void complete(Exchange exchange, @Body ProcessInstanceCompleteRequest request) {
        try {
            processManagementService.complete(exchange,request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_APPROVE_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceApproveResponse approve(Exchange exchange, @Body ProcessInstanceApproveRequest request) {
        try {
            return processManagementService.approve(exchange,request);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }
}
