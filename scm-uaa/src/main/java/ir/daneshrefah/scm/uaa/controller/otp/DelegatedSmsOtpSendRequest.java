package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import lombok.Data;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
@Data
public class DelegatedSmsOtpSendRequest {

    private String recipient;
    private OtpReason reason;
    private String recipientId;
    private UserIdentifierType recipientIdType;
    private String terminalCode;
    private String accessParameter;

}
