package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.domain.otp.AuthenticationMethodType;
import lombok.Data;

@Data
public class ChangeAuthenticationMethodRequest implements RequestData {
       private AuthenticationMethod authenticationMethod;
       private AuthenticationMethodType authenticationMethodType;
       private String nationalCode;
       private String terminalCode;
       private String subOrganization;
}
