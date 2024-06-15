package ir.daneshrefah.scm.process.service.process;

import com.fasterxml.jackson.core.JsonProcessingException;
import ir.daneshrefah.scm.process.model.request.CancelProcessRequest;
import ir.daneshrefah.scm.process.model.request.ProcessStartRequest;
import ir.daneshrefah.scm.process.model.response.ProcessResponse;

public interface ProcessManagement {

   ProcessResponse startProcess(ProcessStartRequest processRequest) throws JsonProcessingException;
   boolean cancelProcess(CancelProcessRequest cancelProcessRequest) throws Exception;
}
