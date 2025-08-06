package ir.daneshrefah.scm.common.log.entity.transaction;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "TRANSACTION_LOG", schema = "REF")
public class TransactionLogEntity {

//    @EmbeddedId
//    private TransactionLogId id;

    @Id
    @Column(name = "TRANSACTION_LOG_ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionLogId;

    @Column(name = "ARCHIVE_NO")
    private Long archiveNo;

    @Column(name = "TRANSACTION_TYPE")
    private Integer transactionType;

    @Column(name = "CHANNEL_ID")
    private Integer channelId;

    @Column(name = "EB_SERVICE_ID")
    private Integer ebServiceId;

    @Column(name = "TRANSACTION_STATE_ID")
    private Integer transactionStateId;

    @Column(name = "DUPLICATE")
    private Integer duplicate;

    @Column(name = "CSP_CHANNEL_ID")
    private Integer cspChannelId;

    @Column(name = "CSP_USERNAME")
    private String cspUsername;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "ACCOUNT_NO")
    private String accountNo;

    @Column(name = "SERVER_CODE")
    private String serverCode;

    @Column(name = "MESSAGE_SEQUENCE_ID")
    private String messageSequenceId;

    @Column(name = "LOG_TIME")
    private Timestamp logTime;

    @Column(name = "SERVER_EXCEPTION")
    private String serverException;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "MESSAGE")
    private String payload;

    @Column(name = "DOC_NO")
    private String docNo;

    @Column(name = "TERMINAL_TYPE")
    private String terminalType;

    @Column(name = "INTER_BANK")
    private Boolean interBank;

    @Column(name = "AMOUNT")
    private Long amount;

    @Column(name = "TERMINAL_ID")
    private String terminalId;

    @Column(name = "CARD_NO")
    private String cardNo;

    @Column(name = "CLIENT_DATE")
    private Timestamp clientDate;

    @Column(name = "EXTERNAL_SEQUENCE_ID")
    private String externalSequenceId;

    @Column(name = "ORIGINAL_SEQUENCE_ID")
    private String originalSequenceId;

    @Column(name = "CLIENT_IP_ADDRESS")
    private String clientIPAddress;

    @Column(name = "CLIENT_PHONE_NUMBER")
    private String clientPhoneNumber;

    @Column(name = "DESTINATION")
    private String destination;
}
