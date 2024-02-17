package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
@Getter
public class OtpVerifyResponse extends OtpBaseResponse {

    private final Integer tryCount;

    @Builder
    public OtpVerifyResponse(String terminalCode, String accessParameter, String recipientUsername, String recipient, OtpType otpType, OtpReason reason, boolean isSuccessful, Integer tryCount) {
        super(terminalCode, accessParameter, recipientUsername, recipient, otpType, reason, isSuccessful);
        this.tryCount = tryCount;
    }
}
