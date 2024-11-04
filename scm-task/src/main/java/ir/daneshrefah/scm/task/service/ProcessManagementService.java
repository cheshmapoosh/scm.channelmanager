package ir.daneshrefah.scm.task.service;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.task.model.*;

public interface ProcessManagementService {
    ProcessInstanceStartResponse start(ProcessInstanceStartRequest processInstanceStartRequest);

    PagedResponseData<ProcessInstanceResponse> findAll(ProcessInstanceFilterRequest processInstanceFilterRequest);

    ProcessInstanceUpdateResponse updateDescription(ProcessInstanceUpdateRequest request);

    ProcessInstanceEntity findByID(Long processId);

    ProcessInstanceApproveResponse approve(ProcessInstanceApproveRequest request);

    void complete(ProcessInstanceCompleteRequest request);
}
