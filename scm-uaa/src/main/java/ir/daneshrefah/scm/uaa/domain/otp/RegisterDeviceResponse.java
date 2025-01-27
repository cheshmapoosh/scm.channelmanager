package ir.daneshrefah.scm.uaa.domain.otp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RegisterDeviceResponse{
    private boolean success;
    private String serialNo;
    private String resultCode;
}
