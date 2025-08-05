package ir.daneshrefah.scm.common.log.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class TransactionLogResponse {

    private Long transactionLogId;

    private Long archiveNo;

    private Integer transactionType;

    private Integer channelId;

    private String channelCode;

    private Integer ebServiceId;

    private Integer transactionStateId;

    private Integer duplicate;

    private Integer cspChannelId;

    private String cspUsername;

    private String username;

    private String statusCode;

    private String accountNo;

    private String serverCode;

    private String messageSequenceId;
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    private LocalDateTime logTime;

    private String serverException;

    private String description;

    private String docNo;

    private String terminalType;

    private Boolean interBank;

    private Long amount;

    private String terminalId;

    private String cardNo;

    private LocalDateTime clientDate;

    private String externalSequenceId;

    private String originalSequenceId;

    private String clientIPAddress;

    private String clientPhoneNumber;

    private String destination;
}
