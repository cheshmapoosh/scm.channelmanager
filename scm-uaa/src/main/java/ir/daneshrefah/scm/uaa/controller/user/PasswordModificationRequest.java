package ir.daneshrefah.scm.uaa.controller.user;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

@Data
public class PasswordModificationRequest implements RequestData {
    private String username;
    private String terminalCode;
    private String oldPassword;
    private String newPassword;
}
