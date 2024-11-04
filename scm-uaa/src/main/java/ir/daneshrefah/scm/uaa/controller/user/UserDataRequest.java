package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@Data
public class UserDataRequest implements RequestData {

    private String nickname;
    private String terminalCode;
    private AuthenticationMethod loginAuthenticationMethod;
    private AuthenticationMethod transactionAuthenticationMethod;
    private Set<String> accessParameters;
    private Boolean active;
    private String loginStaticPassword;
    private String transactionStaticPassword;
    private String otpSerialNumber;
    private Long personId;
    private String creatorBranch;

}
