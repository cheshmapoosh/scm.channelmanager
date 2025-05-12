package ir.daneshrefah.scm.otp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VerifyOTOResponse {
    private boolean isSuccessful;
    private String errors;
    private OtpType otpType;
}
