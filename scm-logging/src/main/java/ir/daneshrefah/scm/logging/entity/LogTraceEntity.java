package ir.daneshrefah.scm.logging.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "TBL_LOG_TRACE", schema = "REF")
public class LogTraceEntity {
    @EmbeddedId
    private LogPrimaryKey logPrimaryKey;
    @Column(name = "CHANNEL_CODE")
    private String channelCode;
    @Column(name = "TERMINAL_CODE")
    private String terminalCode;
    @Column(name = "CLIENT_ID")
    private String clientId;
    @Column(name = "CORROLATION_ID")
    private String correlationId;
    @Column(name = "CLIENT_CORROLATION_ID")
    private String clientCorrelationId;
    @Column(name = "FLOW_ID")
    private String flowId;
    @Column(name = "MESSAGE_ID")
    private String messageId;
    @Column(name = "SPAN_STATUS")
    private String spanStatus;
    @Column(name = "SERVICE_CODE")
    private String serviceCode;
    @Column(name = "NICKNAME")
    private String nickname;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "DELEGATOR_USERNAME")
    private String delegatorUsername;
    @Column(name = "DELEGATOR_NICKNAME")
    private String delegatorNickname;
    @Column(name = "HOST_ADDRESS")
    private String hostAddress;
    @Column(name = "SPAN_KIND")
    private String spanKind;
    @Column(name = "MESSAGE_STATUS")
    private String messageStatus;
    @Column(name = "SPAN_NAME")
    private String spanName;
    @Column(name = "START_TIME")
    private Date startTime;
    @Column(name = "END_TIME")
    private Date endTime;
    @Column(name = "CLIENT_IP_ADDRESS")
    private String clientIpAddress;
    @Column(name = "VERSION")
    private String  version;
    @Column(name = "PROVIDER_CODE")
    private String providerCode;
    @Column(name = "PROVIDER_RESPONSE_CODE")
    private String providerResponseCode;
    @Column(name = "RESPONSE_STATUS_CODE")
    private Integer statusCode;
    @Column(name = "EXCEPTION_CLASS_NAME")
    private String exceptionClassName;
    @Column(name = "ENDPOINT")
    private String endPoint;
    @Column(name = "AMOUNT")
    private String amount;
    @Column(name = "ACCOUNT_NO")
    private String accountNo;
    @Column(name = "CARD_NO")
    private String cardNo;
    @Column(name = "PARENT_SPAN_ID")
    private String parentSpanId;
    @Column(name = "PAYLOAD")
    @Basic(fetch = FetchType.LAZY)
    private String payload;
    @Column(name = "ARCHIVE_NO", updatable = false)
    private Long archiveNo;
    @Column(name = "ROW_NO")
    private Integer rowNo;
}
