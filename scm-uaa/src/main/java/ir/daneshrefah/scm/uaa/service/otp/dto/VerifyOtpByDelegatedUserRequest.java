package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpByDelegatedUserRequest extends VerifyOTP {
    private String recipient;
    private OtpReason reason;
    private String recipientId;
    private UserIdentifierType recipientIdType;
    private String terminalCode;
    private String accessParameter;
}
