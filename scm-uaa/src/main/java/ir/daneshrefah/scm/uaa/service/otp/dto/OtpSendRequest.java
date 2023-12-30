package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.uaa.domain.notification.NotificationType;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import lombok.Getter;
import lombok.Setter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@Setter
public class OtpSendRequest extends OtpBaseRequest {

    private NotificationType notificationType;

}
