package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

@Data
public class PasswordSmsOtpVerifyRequest implements RequestData {
    private String username;
    private String otpCode;
}
