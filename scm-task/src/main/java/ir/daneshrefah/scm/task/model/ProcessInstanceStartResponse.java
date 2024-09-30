package ir.daneshrefah.scm.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import lombok.Data;

@Data
public class ProcessInstanceStartResponse {
    private Long id;
    private String accountNo;
    private ProcessStatusEnum status;
    private String statusName;
    private Long amount;
    private String description;
    private String destination;
    private String createAt;
    private ProcessCodeEnum processCode;
    private JsonNode transactionData;
}
