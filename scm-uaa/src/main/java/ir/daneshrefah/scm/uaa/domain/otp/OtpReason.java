package ir.daneshrefah.scm.uaa.domain.otp;

import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Getter
@RequiredArgsConstructor
public enum OtpReason {

    AUTHENTICATION(2, 4, OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1),
    CHANGE_LOGIN_AUTHENTICATION_METHOD(2, 4, OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1),
    CHANGE_TRANSACTION_AUTHENTICATION_METHOD(2, 4, OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1),
    ACTIVATION(2, 4, OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1),
    ACH(2, 4, OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1);

    private final int timeToLiveMinutes;
    private final int length;
    private final OtpPattern pattern;
    private final NotificationTemplate notificationTemplate;
    private final int maxReusedCount;
    private final int maxFailedCount;

}
