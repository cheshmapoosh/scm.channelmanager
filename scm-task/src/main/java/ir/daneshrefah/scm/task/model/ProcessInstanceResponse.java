package ir.daneshrefah.scm.task.model;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import lombok.Data;

import java.util.List;
import java.util.Objects;

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
    private IssuerModel createdBy;
    private ProcessCodeEnum processCode;
    private JsonNode transactionData;
    private String correlationId;
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
