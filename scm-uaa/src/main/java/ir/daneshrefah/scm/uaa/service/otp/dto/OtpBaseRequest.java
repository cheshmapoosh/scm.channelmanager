package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@AllArgsConstructor
public abstract class OtpBaseRequest {

    private OtpType otpType;
    private OtpReason reason;
    private Recipient recipient;
    private IssuerInfo issuer;
    private final Instant receiveTime = Instant.now();

}
