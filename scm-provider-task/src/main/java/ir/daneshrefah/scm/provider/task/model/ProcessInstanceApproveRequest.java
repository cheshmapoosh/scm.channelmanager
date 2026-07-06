package ir.daneshrefah.scm.provider.task.model;

import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessNameEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceApproveRequest {
    private Long id;
    private String correlationId;
    private ProcessNameEnum processName;
    private ProcessCodeEnum processCode;
}
