package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
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
