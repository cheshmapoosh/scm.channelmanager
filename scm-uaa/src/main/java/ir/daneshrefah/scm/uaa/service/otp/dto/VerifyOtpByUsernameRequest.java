package ir.daneshrefah.scm.uaa.service.otp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpByUsernameRequest extends VerifyOTP {
    private String username;
}
