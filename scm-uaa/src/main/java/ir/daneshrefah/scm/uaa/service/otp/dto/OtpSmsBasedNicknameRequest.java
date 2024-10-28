package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.controller.otp.SmsOtpSendRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpSmsBasedNicknameRequest extends SmsOtpSendRequest {
    private String nickname;
}
