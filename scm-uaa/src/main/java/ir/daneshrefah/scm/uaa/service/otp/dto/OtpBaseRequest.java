package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@SuperBuilder
@Setter
@Getter
@NoArgsConstructor
public abstract class OtpBaseRequest {
    private OtpType otpType;
    private OtpReason reason;
    private Recipient recipient;
    private final Instant receiveTime = Instant.now();
}
