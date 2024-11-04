package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

import java.util.Set;

@Data
public class UserDataChangeRequest implements RequestData {
    private Integer id;
    private String nickname;
    private AuthenticationMethod loginAuthenticationMethod;
    private AuthenticationMethod transactionAuthenticationMethod;
    private Set<String> accessParameters;
    private Boolean active;
    private String loginStaticPassword;
    private String transactionStaticPassword;
    private String otpSerialNumber;
}
