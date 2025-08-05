package ir.daneshrefah.scm.common.log.model;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class LogTraceRequest extends PagedRequestData {
    private String terminalCode;
    private String correlationId;
    private String clientCorrelationId;
    private String serviceCode;
    private String nickname;
    private String username;
    private String delegatorUsername;
    private String delegatorNickname;
    private Integer statusCode;
    private Timestamp startTime;
    private String amount;
    private String accountNo;
    private String cardNo;
}
