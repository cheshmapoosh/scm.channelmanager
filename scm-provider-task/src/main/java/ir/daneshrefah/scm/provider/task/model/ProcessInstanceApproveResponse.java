package ir.daneshrefah.scm.provider.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import lombok.Data;

import java.util.List;

@Data
public class ProcessInstanceApproveResponse {
    private Long id;
    private String executionId;
    private String accountNo;
    private ProcessStatusEnum processStatus;
    private String statusName;
    private Long amount;
    private String description;
    private String destination;
    private String createAt;
    private ProcessCodeEnum processCode;
    private JsonNode transactionData;
    private JsonNode attribute;
    private String correlationId;
    private List<TaskResponse> tasks;
    private List<UserModel> users;
    private UserModel confirmUser;
}
