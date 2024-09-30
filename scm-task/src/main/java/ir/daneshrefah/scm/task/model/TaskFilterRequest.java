package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import ir.daneshrefah.scm.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskFilterRequest extends PagedRequestData {
    private TaskStatusEnum status;
    private String serviceCode;
    private String accountNo;
    private Long fromDate;
    private Long toDate;
    private Integer userId;
    private ProcessCodeEnum transactionType;
    private String nationalId;//TODO remove it
}
