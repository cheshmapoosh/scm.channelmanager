package ir.daneshrefah.scm.uaa.domain.otp;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OtpAssignRequest {
    private String nationalCode;
    private OTPState otpState;
    private String otpSerialNo;
}
