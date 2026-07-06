package ir.daneshrefah.scm.provider.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessNameEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProcessInstanceStartRequest extends ProcessInstanceRequest {
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