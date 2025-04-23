package ir.daneshrefah.scm.otp.dto;

import ir.daneshrefah.scm.common.constant.otp.OtpType;
import lombok.Data;

@Data
public class VerifyOTOResponse {
    private boolean isSuccessful;
    private String errors;
    private OtpType otpType;
}
