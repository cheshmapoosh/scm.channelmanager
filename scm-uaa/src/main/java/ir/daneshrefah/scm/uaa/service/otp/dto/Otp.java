package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@Builder
public class Otp implements Serializable {

    private final String key;
    private final OtpType otpType;
    private final OtpReason reason;
    private final Recipient recipient;
    private final IssuerInfo issuer;
    private final String otpCode;
    private final Instant expireTime;
    @Setter
    private boolean isDelivered;
    @Setter
    private int retryCount;

}
