package ir.daneshrefah.scm.common.log.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionPayloadResponse {

    private String description;

    private String message;

    private String docNo;

    private String cardNo;

    private LocalDateTime clientDate;

    private String externalSequenceId;

    private String originalSequenceId;

    private String clientIPAddress;

    private String clientPhoneNumber;

    private String destination;
}
