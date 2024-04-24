package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import lombok.Builder;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
public class OtpSendRequest extends OtpBaseRequest {

    private final String issuerAddress;
    private final Authentication issuerUser;

    @Builder
    public OtpSendRequest(String terminalCode, String accessParameter, String recipientUsername, String recipient, OtpType otpType, OtpReason reason, String issuerAddress, Authentication issuerUser) {
        super(terminalCode, accessParameter, recipientUsername, recipient, otpType, reason);
        this.issuerAddress = issuerAddress;
        this.issuerUser = issuerUser;
    }
}
