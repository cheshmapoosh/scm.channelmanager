package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.otp.OtpAuthenticationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpByNicknameRequest extends VerifyOTP {
    private String nickname;
    private final static OtpAuthenticationType OTP_AUTHENTICATION_TYPE_DEFAULT = OtpAuthenticationType.TRANSACTION;
    private OtpAuthenticationType otpAuthenticationType = OTP_AUTHENTICATION_TYPE_DEFAULT;
}
