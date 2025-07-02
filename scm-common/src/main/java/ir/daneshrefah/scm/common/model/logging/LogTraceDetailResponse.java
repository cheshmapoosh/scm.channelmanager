package ir.daneshrefah.scm.common.model.logging;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class LogTraceDetailResponse {
    private String spanId;
    private String traceId;
    private String channelCode;
    private String terminalCode;
    private String clientId;
    private String correlationId;
    private String clientCorrelationId;
    private String flowId;
    private String messageId;
    private String spanStatus;
    private String clientIpAddress;
    private String serviceCode;
    private String nickname;
    private String username;
    private String delegatorUsername;
    private String delegatorNickname;
    private String hostAddress;
    private String spanKind;
    private String messageStatus;
    private Date startTime;
    private Date endTime;
    private String version;
    private String providerCode;
    private String providerResponseCode;
    private Integer statusCode;
    private String exceptionClassName;
    private String endPoint;
    private String amount;
    private String accountNo;
    private String cardNo;
    private String parentSpanId;
    private String payload;
}
