package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.uaa.domain.otp.AuthenticationMethodType;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    private String nationalCode;
    private String subOrganizationId;
    private String newPassword;
    private AuthenticationMethodType authenticationMethodType;
    private String terminalCode;
}
