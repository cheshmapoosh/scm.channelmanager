package ir.daneshrefah.scm.logging.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionLogResponse {
    private Long id;
    private String channelCode;
    private String terminalCode;
    private String clientId;
    private String correlationId;
    private String clientCorrelationId;
    private String messageId;
    private Integer statusCode;
    private String nickname;
    private String username;
    private String delegatorUsername;
    private String endPoint;
    private String amount;
    private String accountNo;
    private String cardNo;
    private Date startTime;
    private Date endTime;
    private Long durationMills;
}
