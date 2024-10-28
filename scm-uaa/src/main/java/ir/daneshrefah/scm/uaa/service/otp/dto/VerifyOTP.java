package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOTP {
    private OtpReason reason;
    private String claimCode;
    private OtpType otpType;
}