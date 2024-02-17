package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
@Getter
public class OtpSendResponse extends OtpBaseResponse {

    private final String otpCode;
    private final Instant expireTime;

    @Builder
    public OtpSendResponse(String terminalCode, String accessParameter, String recipientUsername, String recipient, OtpType otpType, OtpReason reason, boolean isSuccessful, String otpCode, Instant expireTime) {
        super(terminalCode, accessParameter, recipientUsername, recipient, otpType, reason, isSuccessful);
        this.otpCode = otpCode;
        this.expireTime = expireTime;
    }

}
