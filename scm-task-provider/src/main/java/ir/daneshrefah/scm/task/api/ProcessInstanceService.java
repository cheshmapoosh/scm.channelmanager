package ir.daneshrefah.scm.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.task.event.TaskProviderEventPublisher;
import ir.daneshrefah.scm.task.model.*;
import ir.daneshrefah.scm.task.service.ProcessManagementService;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;
import static ir.daneshrefah.scm.common.event.provider.ScmProviderEventType.*;

@Service
public class ProcessInstanceService extends AbstractJavaService {

    private final ProcessManagementService processManagementService;
    private final TaskProviderEventPublisher eventPublisher;

    public ProcessInstanceService(
            ServiceProducerTemplate producerTemplate,
            ObjectMapper objectMapper,
            ProcessManagementService processManagementService,
            TaskProviderEventPublisher eventPublisher
    ) {
        super(producerTemplate, objectMapper);
        this.processManagementService = processManagementService;
        this.eventPublisher = eventPublisher;
    }

    @JavaService(operationCode = SVC_CARTABLE_START_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceStartResponse start(Exchange exchange, @Body ProcessInstanceStartRequest processInstanceStartRequest) {
        eventPublisher.publishProcess(PROCESS_START_REQUESTED, exchange, null,
                processInstanceStartRequest.getProcessCode(), null, SVC_CARTABLE_START_PROCESS.name(), null);
        try {
            ProcessInstanceStartResponse response = processManagementService.start(exchange, processInstanceStartRequest);
            eventPublisher.publishProcess(PROCESS_STARTED, exchange, response.getId(),
                    response.getProcessCode(), response.getStatus(), SVC_CARTABLE_START_PROCESS.name(), null);
            return response;
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, null,
                    processInstanceStartRequest.getProcessCode(), null, SVC_CARTABLE_START_PROCESS.name(), exception);
            throw exception;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_GET_ALL_PROCESS)
    @SuppressWarnings("unused")
    public PagedResponseData<ProcessInstanceResponse> findAll(Exchange exchange, @Body ProcessInstanceFilterRequest processInstanceFilterRequest) {
        return processManagementService.findAll(exchange, processInstanceFilterRequest);
    }

    @JavaService(operationCode = SVC_CARTABLE_UPDATE_PROCESS_DESCRIPTION)
    @SuppressWarnings("unused")
    public ProcessInstanceUpdateResponse updateDescription(Exchange exchange, @Body ProcessInstanceUpdateRequest request) {
        return processManagementService.updateDescription(exchange, request);
    }

    @JavaService(operationCode = SVC_CARTABLE_CANCEL_PROCESS)
    @SuppressWarnings("unused")
    public void cancelProcess(Exchange exchange, @Body ProcessInstanceCancelRequest request) {
        eventPublisher.publishProcess(PROCESS_CANCEL_REQUESTED, exchange, request.getId(),
                null, null, SVC_CARTABLE_CANCEL_PROCESS.name(), null);
        try {
            processManagementService.cancelProcess(exchange, request);
            eventPublisher.publishProcess(PROCESS_CANCELLED, exchange, request.getId(),
                    null, "CANCEL", SVC_CARTABLE_CANCEL_PROCESS.name(), null);
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, request.getId(),
                    null, null, SVC_CARTABLE_CANCEL_PROCESS.name(), exception);
            throw exception;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_COMPLETE_PROCESS)
    @SuppressWarnings("unused")
    public void complete(Exchange exchange, @Body ProcessInstanceCompleteRequest request) {
        eventPublisher.publishProcess(PROCESS_COMPLETE_REQUESTED, exchange, request.getId(),
                null, request.getStatus(), SVC_CARTABLE_COMPLETE_PROCESS.name(), null);
        try {
            processManagementService.complete(exchange, request);
            eventPublisher.publishProcess(PROCESS_COMPLETED, exchange, request.getId(),
                    null, request.getStatus(), SVC_CARTABLE_COMPLETE_PROCESS.name(), null);
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, request.getId(),
                    null, request.getStatus(), SVC_CARTABLE_COMPLETE_PROCESS.name(), exception);
            throw exception;
        }
    }

    @JavaService(operationCode = SVC_CARTABLE_APPROVE_PROCESS)
    @SuppressWarnings("unused")
    public ProcessInstanceApproveResponse approve(Exchange exchange, @Body ProcessInstanceApproveRequest request) {
        eventPublisher.publishProcess(PROCESS_APPROVE_REQUESTED, exchange, request.getId(),
                request.getProcessCode(), null, SVC_CARTABLE_APPROVE_PROCESS.name(), null);
        try {
            ProcessInstanceApproveResponse response = processManagementService.approve(exchange, request);
            eventPublisher.publishProcess(PROCESS_APPROVED, exchange, response.getId(),
                    response.getProcessCode(), response.getProcessStatus(), SVC_CARTABLE_APPROVE_PROCESS.name(), null);
            return response;
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, request.getId(),
                    request.getProcessCode(), null, SVC_CARTABLE_APPROVE_PROCESS.name(), exception);
            throw exception;
        }
    }
}
