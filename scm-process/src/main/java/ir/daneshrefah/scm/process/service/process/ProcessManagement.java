package ir.daneshrefah.scm.process.service.process;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.process.ProcessCancelRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;

public interface ProcessManagement {
   ProcessStartResponse startProcess(ProcessStartRequest processStartRequest) throws Exception;
   boolean cancelProcess(ProcessCancelRequest processCancelRequest) throws Exception;
   PagedResponseData<ProcessInstanceResponse> getActiveProcess(ProcessInstanceRequest request);
   Long activeCount(ProcessInstanceRequest request);
}
