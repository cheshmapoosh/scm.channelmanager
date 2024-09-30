package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceCompleteRequest {
    private Long id;
    private ProcessStatusEnum status;
    private String nationalId;//TODO remove it
}
