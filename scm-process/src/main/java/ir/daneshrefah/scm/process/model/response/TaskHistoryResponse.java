package ir.daneshrefah.scm.process.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@NoArgsConstructor
public class TaskHistoryResponse {
    private String id;
    private String owner;
    private String assignee;
    private String parentTaskId;
    private String taskName;
    private String taskPersianName;
    private String description;
    private Date startTime;
    private Long startTimeMillis;
    private Date endTime;
    private Long endTimeMillis;
    private String executionId;
    private String processInstanceId;
    private String rootProcessInstanceId;
    private String taskDefinitionKey;
    private String processDefinitionKey;
    private boolean isDeleted;
    private String deleteReason;
    private String state;
    private String stateName;
    private Map<String,Object> data;
}
