package ir.daneshrefah.scm.process.service.processInstance;

import ir.daneshrefah.scm.process.model.response.ProcessInstanceResponse;

import java.util.List;

public interface ProcessInstanceManagement {

   List<ProcessInstanceResponse> processInstanceList();
   List<ProcessInstanceResponse> processInstanceDetailList(String deploymentId);
}
