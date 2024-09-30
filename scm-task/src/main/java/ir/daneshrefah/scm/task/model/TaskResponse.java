package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskResponse {
    private Long id;
    private String fullName;
    private String createAt;
    private String updateAt;
    private TaskStatusEnum taskStatus;
    private String statusName;
    private boolean global;
    private ProcessInstanceResponse processInstance;
}
