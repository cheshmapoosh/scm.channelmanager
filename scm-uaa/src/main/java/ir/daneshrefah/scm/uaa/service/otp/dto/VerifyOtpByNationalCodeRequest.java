package ir.daneshrefah.scm.uaa.service.otp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpByNationalCodeRequest extends VerifyOTP {
    private String nationalCode;
    private String subOrganizationId;
}
