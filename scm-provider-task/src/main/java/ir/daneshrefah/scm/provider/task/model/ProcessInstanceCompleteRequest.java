package ir.daneshrefah.scm.provider.task.model;

import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceCompleteRequest extends ProcessInstanceRequest {
    private Long id;
    private ProcessStatusEnum status;
}
