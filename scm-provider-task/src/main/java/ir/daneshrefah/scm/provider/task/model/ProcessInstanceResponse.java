package ir.daneshrefah.scm.provider.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class ProcessInstanceResponse {
    private Long id;
    private String executionId;
    private String accountNo;
    private ProcessStatusEnum processStatus;
    private String statusName;
    private Long amount;
    private String description;
    private String destination;
    private String createAt;
    private IssuerModel createdBy;
    private ProcessCodeEnum processCode;
    private JsonNode transactionData;
    private JsonNode attribute;
    private String correlationId;
    private Long confirmUserId;
    private List<TaskResponse> tasks;
    private boolean canCancel;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessInstanceResponse that = (ProcessInstanceResponse) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
