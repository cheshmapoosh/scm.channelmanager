package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.otp.OtpAuthenticationType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpByUsernameRequest extends VerifyOTP {
    private String username;
    private final static OtpAuthenticationType OTP_AUTHENTICATION_TYPE_DEFAULT = OtpAuthenticationType.TRANSACTION;
    private OtpAuthenticationType otpAuthenticationType = OTP_AUTHENTICATION_TYPE_DEFAULT;
}
