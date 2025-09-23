package ir.daneshrefah.scm.common.log.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LogTraceResponse {
    private String spanId;
    private String traceId;
    private String channelCode;
    private String terminalCode;
    private String clientId;
    private String correlationId;
    private String endPoint;
    private Integer statusCode;
    private String nickname;
    private String username;
    private String delegatorUsername;
    private String amount;
    private String accountNo;
    private String cardNo;
    private Date startTime;
    private Date endTime;
    private Long durationMillis;
    private String clientCorrelationId;
    private String flowId;
    private String messageId;
    private String spanStatus;
    private String delegatorNickname;
    private String hostAddress;
    private String spanKind;
    private String messageStatus;
    private String spanName;
    private String clientIpAddress;
    private String version;
    private String providerCode;
    private String providerResponseCode;
    private String exceptionClassName;
    private String parentSpanId;


}
