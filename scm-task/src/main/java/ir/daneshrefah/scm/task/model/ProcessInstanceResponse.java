package ir.daneshrefah.scm.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import lombok.Data;

import java.util.List;

@Data
public class ProcessInstanceResponse {
    private Long id;
    private String accountNo;
    private ProcessStatusEnum processStatus;
    private String statusName;
    private Long amount;
    private String description;
    private String destination;
    private String createAt;
    private ProcessCodeEnum processCode;
    private JsonNode transactionData;
    private String correlationId;
    private List<TaskResponse> tasks;
}
