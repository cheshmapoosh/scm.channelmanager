package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@NoArgsConstructor
public class OtpVerifyRequest extends OtpBaseRequest {

    private String claimCode;

    @Builder
    public OtpVerifyRequest(String terminalCode, String accessParameter, String recipientUsername, String recipient, OtpType otpType, OtpReason reason, String claimCode) {
        super(terminalCode, accessParameter, recipientUsername, recipient, otpType, reason);
        this.claimCode = claimCode;
    }

}
