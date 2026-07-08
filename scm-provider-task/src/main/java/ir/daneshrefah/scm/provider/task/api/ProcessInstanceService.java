package ir.daneshrefah.scm.provider.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import ir.daneshrefah.scm.provider.task.event.TaskProviderEventPublisher;
import ir.daneshrefah.scm.provider.task.model.*;
import ir.daneshrefah.scm.provider.task.service.ProcessManagementService;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRole;
import org.apache.camel.Body;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Service;

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

    @SuppressWarnings("unused")
    public ProcessInstanceStartResponse start(Exchange exchange, @Body ProcessInstanceStartRequest processInstanceStartRequest) {
        String operationName = TaskWorkflowRole.START_PROCESS.name();
        eventPublisher.publishProcess(PROCESS_START_REQUESTED, exchange, null,
                processInstanceStartRequest.getProcessCode(), null, operationName, null);
        try {
            ProcessInstanceStartResponse response = processManagementService.start(exchange, processInstanceStartRequest);
            eventPublisher.publishProcess(PROCESS_STARTED, exchange, response.getId(),
                    response.getProcessCode(), response.getStatus(), operationName, null);
            return response;
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, null,
                    processInstanceStartRequest.getProcessCode(), null, operationName, exception);
            throw exception;
        }
    }

    @SuppressWarnings("unused")
    public PagedResponseData<ProcessInstanceResponse> findAll(Exchange exchange, @Body ProcessInstanceFilterRequest processInstanceFilterRequest) {
        return processManagementService.findAll(exchange, processInstanceFilterRequest);
    }

    @SuppressWarnings("unused")
    public ProcessInstanceUpdateResponse updateDescription(Exchange exchange, @Body ProcessInstanceUpdateRequest request) {
        return processManagementService.updateDescription(exchange, request);
    }

    @SuppressWarnings("unused")
    public void cancelProcess(Exchange exchange, @Body ProcessInstanceCancelRequest request) {
        String operationName = TaskWorkflowRole.CANCEL_PROCESS.name();
        eventPublisher.publishProcess(PROCESS_CANCEL_REQUESTED, exchange, request.getId(),
                null, null, operationName, null);
        try {
            processManagementService.cancelProcess(exchange, request);
            eventPublisher.publishProcess(PROCESS_CANCELLED, exchange, request.getId(),
                    null, "CANCEL", operationName, null);
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, request.getId(),
                    null, null, operationName, exception);
            throw exception;
        }
    }

    @SuppressWarnings("unused")
    public void complete(Exchange exchange, @Body ProcessInstanceCompleteRequest request) {
        String operationName = TaskWorkflowRole.COMPLETE_PROCESS.name();
        eventPublisher.publishProcess(PROCESS_COMPLETE_REQUESTED, exchange, request.getId(),
                null, request.getStatus(), operationName, null);
        try {
            processManagementService.complete(exchange, request);
            eventPublisher.publishProcess(PROCESS_COMPLETED, exchange, request.getId(),
                    null, request.getStatus(), operationName, null);
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, request.getId(),
                    null, request.getStatus(), operationName, exception);
            throw exception;
        }
    }

    @SuppressWarnings("unused")
    public ProcessInstanceApproveResponse approve(Exchange exchange, @Body ProcessInstanceApproveRequest request) {
        String operationName = TaskWorkflowRole.APPROVE_PROCESS.name();
        eventPublisher.publishProcess(PROCESS_APPROVE_REQUESTED, exchange, request.getId(),
                request.getProcessCode(), null, operationName, null);
        try {
            ProcessInstanceApproveResponse response = processManagementService.approve(exchange, request);
            eventPublisher.publishProcess(PROCESS_APPROVED, exchange, response.getId(),
                    response.getProcessCode(), response.getProcessStatus(), operationName, null);
            return response;
        } catch (RuntimeException exception) {
            eventPublisher.publishProcess(PROCESS_FAILED, exchange, request.getId(),
                    request.getProcessCode(), null, operationName, exception);
            throw exception;
        }
    }
}
