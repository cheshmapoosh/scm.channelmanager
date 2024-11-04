package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

@Data
public class UserNickNameModifyRequest implements RequestData {

    private String terminalCode;
    //New username
    private String nickName;
    //Current userName
    private String currentNickName;

}
