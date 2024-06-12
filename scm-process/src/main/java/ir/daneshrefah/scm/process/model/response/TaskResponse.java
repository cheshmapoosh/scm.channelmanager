package ir.daneshrefah.scm.process.model.response;

import ir.daneshrefah.scm.process.model.constant.UserTaskStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class TaskResponse {
    private String taskId;
    private String taskName;
    private String taskPersianName;
    private String taskDescription;
    private String taskDefinitionKey;
    private String description;
    private List<Object> assignment; ////TODO change this to user
    private Date createTime;
    private Long createTimeMillis;
    private UserTaskStatus userTaskStatus;
    private ProcessResponse process;
    private Map<String,Object> data;
}