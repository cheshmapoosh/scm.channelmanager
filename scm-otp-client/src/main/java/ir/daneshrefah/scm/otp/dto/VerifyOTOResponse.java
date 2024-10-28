package ir.daneshrefah.scm.otp.dto;

import lombok.Data;

@Data
public class VerifyOTOResponse {
    private boolean isSuccessful;
    private String errors;
}
