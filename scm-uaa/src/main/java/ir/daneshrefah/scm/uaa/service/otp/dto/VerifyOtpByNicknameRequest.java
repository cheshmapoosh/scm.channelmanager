package ir.daneshrefah.scm.uaa.service.otp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpByNicknameRequest extends VerifyOTP {
    private String nickname;
}
