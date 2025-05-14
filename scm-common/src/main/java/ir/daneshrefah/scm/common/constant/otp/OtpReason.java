package ir.daneshrefah.scm.common.constant.otp;

import ir.daneshrefah.scm.common.model.notification.constants.NotificationTemplate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

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

    AUTHENTICATION                                  (2, 4,OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1,1),
    CHANGE_LOGIN_AUTHENTICATION_METHOD              (2, 4,OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1,2),
    CHANGE_TRANSACTION_AUTHENTICATION_METHOD        (2, 4,OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1,3),
    ACTIVATION                                      (2, 4,OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1,4),
    ACH                                             (2, 4,OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1,5),
    PAYMENT_TRANSFER_INTERNAL                       (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,6),
    PAYMENT_TRANSFER_ACH                            (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,7),
    PAYMENT_TRANSFER_RTGS                           (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,8),
    PAYMENT_TRANSFER_POL                            (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,9),
    PAYMENT_TRANSFER_RECURRING_ADD                  (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,10),
    PAYMENT_TRANSFER_RECURRING_EDIT                 (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,11),
    PAYMENT_TRANSFER_BILL_PAYMENT                   (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,12),
    PAYMENT_TRANSFER_INSURANCE_PAYMENT              (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,13),
    PAYMENT_TRANSFER_PAYMENT_ORDER_INTERNAL         (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,14),
    PAYMENT_TRANSFER_PAYMENT_ORDER_ACH              (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,15),
    PAYMENT_TRANSFER_PAYMENT_ORDER_RTGS             (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,16),
    PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_ADD       (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,17),
    PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_DELETE    (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,18),
    PAYMENT_TRANSFER_ACH_BATCH                      (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,19),
    PAYMENT_TRANSFER_BILL_BATCH                     (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,20),
    PAYMENT_TRANSFER_INTERNAL_BATCH                 (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,21),
    PAYMENT_TRANSFER_INSURANCE_BATCH                (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,22),
    PAYMENT_TRANSFER_RECURRING_ADD_ACH              (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,23),
    PAYMENT_TRANSFER_RECURRING_ADD_BATCH_ACH        (1,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,24),
    BANK_CONSOLE_CUSTOMER_VERIFICATION              (2,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,2,1,25);

    private final int timeToLiveMinutes;
    private final int length;
    private final OtpPattern pattern;
    private final NotificationTemplate notificationTemplate;
     /** @range (1,n) , 0 means unLimit */
    private final int maxReusedCount;
    private final int maxFailedCount;
    private final Integer code;

    public static OtpReason findByCode(Integer code) {
        return Arrays.stream(OtpReason.values())
                .filter(otpReasonEnum -> otpReasonEnum.getCode().equals(code))
                .findFirst()
                .orElse(null);
    }

}
