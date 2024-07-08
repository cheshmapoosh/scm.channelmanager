package ir.daneshrefah.scm.process.service.processInstance;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceRequest;
import ir.daneshrefah.scm.process.service.dto.processInstance.ProcessInstanceResponse;
import ir.daneshrefah.scm.process.service.util.processInstance.ProcessInstanceService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CamundaProcessInstanceService implements ProcessInstanceManagement {

    private final ProcessInstanceService processInstanceService;

    @Override
    public PagedResponseData<ProcessInstanceResponse> getList(ProcessInstanceRequest request) {
        return processInstanceService.getActiveProcessInstance(request);
    }
    @Override
    public Long activeCount(ProcessInstanceRequest request) {
        return processInstanceService.activeCount(request.getDeploymentId());
    }
}
