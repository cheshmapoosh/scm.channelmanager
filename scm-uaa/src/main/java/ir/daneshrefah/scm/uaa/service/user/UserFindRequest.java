package ir.daneshrefah.scm.uaa.service.user;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-12
 */
@Data
public class UserFindRequest extends PagedRequestData {
    private String nickname;
    private String terminalCode;
    private Boolean active;
    private String creatorBranch;
    private Integer terminalId;
    private String accessParameters;
    private String creator;
    private String editor;
    private String username;
    private String nationalId;
    private String nationalCode;
    private AuthenticationMethod loginAuthenticationMethod;
    private AuthenticationMethod transactionAuthenticationMethod;




}
