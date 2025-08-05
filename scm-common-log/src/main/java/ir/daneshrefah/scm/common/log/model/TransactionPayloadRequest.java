package ir.daneshrefah.scm.common.log.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransactionPayloadRequest {
    private Long transactionLogId;
    private Long archiveNo;
}
