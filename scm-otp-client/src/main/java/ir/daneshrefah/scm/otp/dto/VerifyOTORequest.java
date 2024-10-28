package ir.daneshrefah.scm.otp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyOTORequest {
    private String otpType;
    private String reason;
    private String claimCode;
}