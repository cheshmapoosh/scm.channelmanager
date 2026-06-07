package ir.daneshrefah.scm.common.model.notification.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RequiredArgsConstructor
@Getter
public enum NotificationTemplate {

    AUTHENTICATION                                  ("authentication"),
    AUTHENTICATION_OTP                              ("authentication_otp"),
    ACTIVATION                                      ("activation"),
    GENERAL                                         ("general"),
    RESET_FIRST_PASSWORD                            ("reset_first_password"),
    RESET_SECOND_PASSWORD                           ("reset_second_password"),
    CHANNEL_ACTIVATION_REQUEST                      ("channel_activation_request"),
    CHANNEL_ACTIVATION_SUCCESS                      ("channel_successful_activation"),
    CHANNEL_ACTIVATION_FAILED                       ("channel_failed_activation"),
    LOGIN_BLOCKED_MESSAGE                           ("login_blocked_message"),
    REGISTER_BLOCKED_MESSAGE                        ("register_blocked_message"),
    MB_ACTIVATION_CODE                              ("mb_activation_code"),
    PAYMENT_TRANSFER_INTERNAL                       ("payment_transfer_internal"),
    PAYMENT_TRANSFER_ACH                            ("payment_transfer_ach"),
    PAYMENT_TRANSFER_RTGS                           ("payment_transfer_rtgs"),
    PAYMENT_TRANSFER_POL                            ("payment_transfer_pol"),
    PAYMENT_TRANSFER_RECURRING_ADD                  ("payment_transfer_recurring_add"),
    PAYMENT_TRANSFER_RECURRING_EDIT                 ("payment_transfer_recurring_edit"),
    PAYMENT_TRANSFER_BILL_PAYMENT                   ("payment_transfer_bill_payment"),
    PAYMENT_TRANSFER_INSURANCE_PAYMENT              ("payment_transfer_insurance_payment"),
    PAYMENT_TRANSFER_PAYMENT_ORDER_INTERNAL         ("payment_transfer_payment_order_internal"),
    PAYMENT_TRANSFER_PAYMENT_ORDER_ACH              ("payment_transfer_payment_order_ach"),
    PAYMENT_TRANSFER_PAYMENT_ORDER_RTGS             ("payment_transfer_payment_order_rtgs"),
    PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_ADD       ("payment_transfer_cheque_book_issuance_add"),
    PAYMENT_TRANSFER_CHEQUE_BOOK_ISSUANCE_DELETE    ("payment_transfer_cheque_book_issuance_delete"),
    PAYMENT_TRANSFER_ACH_BATCH                      ("payment_transfer_ach_batch"),
    PAYMENT_TRANSFER_BILL_BATCH                     ("payment_transfer_bill_batch"),
    PAYMENT_TRANSFER_INTERNAL_BATCH                 ("payment_transfer_internal_batch"),
    PAYMENT_TRANSFER_INSURANCE_BATCH                ("payment_transfer_insurance_batch"),
    PAYMENT_TRANSFER_RECURRING_ADD_ACH              ("payment_transfer_recurring_add_ach"),
    PAYMENT_TRANSFER_RECURRING_ADD_BATCH_ACH        ("payment_transfer_recurring_add_batch_ach"),
    INQUIRY                                         ("inquiry"),
    SHAHKAR_AUTHENTICATION                          ("shahkar_authentication");

    private final String value;

    public static NotificationTemplate findByCode(String value) {
        return Arrays.stream(values())
                .filter(templateCode -> templateCode.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

}
