package ir.daneshrefah.scm.provider.task.model;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskFilterRequest extends PagedRequestData {
    private TaskStatusEnum status;
    private String serviceCode;
    private String accountNo;
    private Long fromDate;
    private Long toDate;
    private Integer userId;
    private List<ProcessCodeEnum> transactionType;
}
