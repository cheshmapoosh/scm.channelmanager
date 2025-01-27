package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2025-01-23
 */
@Data
public class UserAssignTerminalRequest extends UserByNationalCodeFindRequest implements RequestData {
    private String nickName;
    private String phoneNumber;
    private AuthenticationMethod transactionAuthenticationMethod;
    private AuthenticationMethod loginAuthenticationMethod;
    private String loginStaticPassword;
    private String transactionStaticPassword;
}