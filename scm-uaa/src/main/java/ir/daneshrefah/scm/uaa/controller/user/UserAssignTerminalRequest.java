package ir.daneshrefah.scm.uaa.controller.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2025-01-23
 */
@Getter
@Setter
public class UserAssignTerminalRequest extends UserByNationalCodeFindRequest implements RequestData {
    @JsonIgnore
    private String nickName;
    private String phoneNumber;
    private AuthenticationMethod transactionAuthenticationMethod;
    private AuthenticationMethod loginAuthenticationMethod;
    private String loginStaticPassword;
    private String transactionStaticPassword;
    private String otpCode;
}