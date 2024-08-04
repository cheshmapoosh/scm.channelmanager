package ir.daneshrefah.scm.log.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "SCM_TRANSACTION_LOG", schema = "REF")
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "CHANNEL_CODE")
    private String channelCode;
    @Column(name = "TERMINAL_CODE")
    private String terminalCode;
    @Column(name = "CLIENT_ID")
    private String clientId;
    @Column(name = "CORRELATION_ID")
    private String correlationId;
    @Column(name = "CLIENT_CORRELATION_ID")
    private String clientCorrelationId;
    @Column(name = "CLIENT_FLOW_ID")
    private String clientFlowId;
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
    @Column(name = "MESSAGE_ID")
    private String messageId;
    @Column(name = "HOST_ADDRESS")
    private String hostAddress;
    @Column(name = "EVENT_TYPE")
    private String eventType;
    @Column(name = "MESSAGE_STATUS")
    private String messageStatus;
    @Column(name = "START_TIME")
    private Date startTime;
    @Column(name = "END_TIME")
    private Date endTime;
    @Column(name = "DURATION_MILLS")
    private Long durationMills;
    @Column(name = "PROVIDER_CODE")
    private String providerCode;
    @Column(name = "PROVIDER_RESPONSE_CODE")
    private String providerResponseCode;
    @Column(name = "EXCEPTION_CLASS_NAME")
    private String exceptionClassName;
    @Column(name = "PAYLOAD")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String payload;
    @Column(name = "AMOUNT")
    private String amount;
    @Column(name = "CARD_NUMBER")
    private String cardNumber;
}

