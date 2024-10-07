package ir.daneshrefah.scm.task.model;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import ir.daneshrefah.scm.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.task.constant.ProcessCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProcessInstanceFilterRequest extends PagedRequestData {
    private ProcessStatusEnum status;
    private String serviceCode;
    private String accountNo;
    private Long fromDate;
    private Long toDate;
    private Integer confirmUserId;
    private ProcessCodeEnum processCode;
    private boolean report;
    private Integer userId;
}
