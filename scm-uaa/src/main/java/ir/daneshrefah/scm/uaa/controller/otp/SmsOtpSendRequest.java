package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import lombok.Data;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@Data
public class SmsOtpSendRequest {
    private OtpReason reason;
    private Map<String,Object> metadata;
}
