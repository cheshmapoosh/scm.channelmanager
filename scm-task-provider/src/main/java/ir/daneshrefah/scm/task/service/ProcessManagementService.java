package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.model.*;
import org.apache.camel.Exchange;

public interface ProcessManagementService {
    ProcessInstanceStartResponse start(Exchange exchange,ProcessInstanceStartRequest processInstanceStartRequest);

    PagedResponseData<ProcessInstanceResponse> findAll(Exchange exchange,ProcessInstanceFilterRequest processInstanceFilterRequest);

    ProcessInstanceUpdateResponse updateDescription(Exchange exchange,ProcessInstanceUpdateRequest request);

    ProcessInstanceEntity findByID(Exchange exchange,Long processId);

    ProcessInstanceApproveResponse approve(Exchange exchange,ProcessInstanceApproveRequest request);

    void complete(Exchange exchange,ProcessInstanceCompleteRequest request);

    void cancelProcess(Exchange exchange,ProcessInstanceCancelRequest request);
}


