package ir.daneshrefah.scm.provider.task.model;

import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private Long taskId;
    private TaskStatusEnum action;
    private ProcessNameEnum processName;
    private ProcessCodeEnum processCode;
}
