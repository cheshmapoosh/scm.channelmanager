package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.uaa.domain.otp.AuthenticationMethodType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOTP {

    private final static AuthenticationMethodType OTP_AUTHENTICATION_TYPE_DEFAULT = AuthenticationMethodType.TRANSACTION;
    private OtpReason reason;
    private String claimCode;
    private OtpType otpType;
    private AuthenticationMethodType authenticationMethodType = OTP_AUTHENTICATION_TYPE_DEFAULT;
}