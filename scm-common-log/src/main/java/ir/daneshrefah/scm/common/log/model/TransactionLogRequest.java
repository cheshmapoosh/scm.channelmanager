package ir.daneshrefah.scm.common.log.model;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class TransactionLogRequest extends PagedRequestData {
    private Long Id;
    private Long nextId;
    private Long previousId;
    private BigDecimal transactionLogId;
    private Integer channelId;
    private Long logTime;
    private Integer archiveNo;
}
