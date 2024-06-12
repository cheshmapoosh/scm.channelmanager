package ir.daneshrefah.scm.process.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProcessResponse {
    private String processDefinitionId;
    private String executionId;
    private String businessKey;
    private String rootProcessInstanceId;
    private String processInstanceId;
    private String caseInstanceId;
    private String processName;
    private LocalDateTime createdTime;
}
