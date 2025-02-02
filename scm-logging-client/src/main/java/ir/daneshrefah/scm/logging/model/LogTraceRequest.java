package ir.daneshrefah.scm.logging.model;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LogTraceRequest extends PagedRequestData {
    private String channelCode;
    private String terminalCode;
    private String clientId;
    private String correlationId;
    private String clientCorrelationId;
    private String clientFlowId;
    private String flowId;
    private String messageId;
    private String parentMessageId;
    private String serviceCode;
    private String nickname;
    private String username;
    private String delegatorUsername;
    private String delegatorNickname;
    private String hostAddress;
    private Integer statusCode;
    private Date startTime;
    private Date endTime;
    private String exceptionClassName;
    private String endPoint;
    private String amount;
    private String accountNo;
    private String cardNo;
}
