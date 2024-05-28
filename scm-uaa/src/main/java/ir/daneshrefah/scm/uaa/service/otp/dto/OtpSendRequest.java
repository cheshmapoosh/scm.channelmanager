package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
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

    @Builder
    public OtpSendRequest(OtpType otpType, OtpReason reason, Recipient recipient, IssuerInfo issuer) {
        super(otpType, reason, recipient, issuer);
    }

}
