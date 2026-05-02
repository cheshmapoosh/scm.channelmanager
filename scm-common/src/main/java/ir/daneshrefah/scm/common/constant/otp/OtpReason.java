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

    AUTHENTICATION                                  (2, 5,OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1,1),
    CHANGE_LOGIN_AUTHENTICATION_METHOD              (2, 4,OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1,2),
    CHANGE_TRANSACTION_AUTHENTICATION_METHOD        (2, 4,OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1,3),
    ACTIVATION                                      (2, 4,OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 1, 1,4),
    ACH                                             (2, 4,OtpPattern.NUMERIC, NotificationTemplate.GENERAL, 2, 1,5),
    PAYMENT_TRANSFER_INTERNAL                       (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_INTERNAL,2,1,6),
    PAYMENT_TRANSFER_ACH                            (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_ACH,2,1,7),
    PAYMENT_TRANSFER_RTGS                           (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_RTGS,2,1,8),
    PAYMENT_TRANSFER_POL                            (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_POL,2,1,9),
    PAYMENT_TRANSFER_RECURRING_ADD                  (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_RECURRING_ADD,2,1,10),
    PAYMENT_TRANSFER_RECURRING_EDIT                 (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_RECURRING_EDIT,2,1,11),
    PAYMENT_TRANSFER_BILL_PAYMENT                   (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_BILL_PAYMENT,2,1,12),
    PAYMENT_TRANSFER_INSURANCE_PAYMENT              (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_INSURANCE_PAYMENT,2,1,13),
    PAYMENT_TRANSFER_PAYMENT_ORDER_INTERNAL         (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_PAYMENT_ORDER_INTERNAL,2,1,14),
    PAYMENT_TRANSFER_PAYMENT_ORDER_ACH              (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_PAYMENT_ORDER_ACH,2,1,15),
    PAYMENT_TRANSFER_PAYMENT_ORDER_RTGS             (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_PAYMENT_ORDER_RTGS,2,1,16),
    PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_ADD       (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_ADD,2,1,17),
    PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_DELETE    (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_DELETE,2,1,18),
    PAYMENT_TRANSFER_ACH_BATCH                      (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_ACH_BATCH,2,1,19),
    PAYMENT_TRANSFER_BILL_BATCH                     (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_BILL_BATCH,2,1,20),
    PAYMENT_TRANSFER_INTERNAL_BATCH                 (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_INTERNAL_BATCH,2,1,21),
    PAYMENT_TRANSFER_INSURANCE_BATCH                (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_INSURANCE_BATCH,2,1,22),
    PAYMENT_TRANSFER_RECURRING_ADD_ACH              (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_RECURRING_ADD_ACH,2,1,23),
    PAYMENT_TRANSFER_RECURRING_ADD_BATCH_ACH        (2,5,OtpPattern.NUMERIC,NotificationTemplate.PAYMENT_TRANSFER_RECURRING_ADD_BATCH_ACH,2,1,24),
    BANK_CONSOLE_CUSTOMER_VERIFICATION              (10,5,OtpPattern.NUMERIC,NotificationTemplate.GENERAL,5,1,25),
    PROCUREMENT_AGENT_PERMISSION                    (5, 5, OtpPattern.NUMERIC, NotificationTemplate.AUTHENTICATION_OTP, 1, 1, 26),
    INQUIRY_COMMISSION                              (2,5,OtpPattern.NUMERIC,NotificationTemplate.INQUIRY,2,1,26),
    SHAHKAR_AUTHENTICATION              (10,5,OtpPattern.NUMERIC,NotificationTemplate.SHAHKAR_AUTHENTICATION,5,1,26),
    ;

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
