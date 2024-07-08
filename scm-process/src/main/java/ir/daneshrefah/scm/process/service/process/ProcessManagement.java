package ir.daneshrefah.scm.process.service.process;

import ir.daneshrefah.scm.process.service.dto.process.ProcessCancelRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartRequest;
import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;

public interface ProcessManagement {
   ProcessStartResponse startProcess(ProcessStartRequest processStartRequest) throws Exception;
   boolean cancelProcess(ProcessCancelRequest processCancelRequest) throws Exception;
}
