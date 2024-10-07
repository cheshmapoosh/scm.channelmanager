package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {
    private Long taskId;
    private TaskStatusEnum action;
}
