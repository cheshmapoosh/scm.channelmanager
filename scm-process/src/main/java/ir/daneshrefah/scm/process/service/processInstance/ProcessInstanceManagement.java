package ir.daneshrefah.scm.process.service.processInstance;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;

public interface ProcessInstanceManagement {
    PagedResponseData<ProcessInstanceResponse> getList(ProcessInstanceRequest request);
    Long activeCount(ProcessInstanceRequest request);
}
