package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public abstract class OtpBaseRequest {

    private String terminalCode;
    private String accessParameter;
    private String recipientUsername;
    private String recipient;
    private OtpType otpType;
    private OtpReason reason;

}
