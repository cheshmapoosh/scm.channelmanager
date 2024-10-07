package ir.daneshrefah.scm.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.task.constant.ProcessNameEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessInstanceStartRequest {
    private String accountNo;
    private UserModel confirmUser;
    private List<UserModel> users;
    private JsonNode transactionData;
    private ProcessCodeEnum processCode;
    private Long amount;
    private String destination;
    private String description;
    private ProcessNameEnum processName;
}