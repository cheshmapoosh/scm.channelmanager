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

    AUTHENTICATION("authentication"),
    AUTHENTICATION_OTP("authentication_otp"),
    ACTIVATION("activation"),
    GENERAL("general"),
    RESET_FIRST_PASSWORD("reset_first_password"),
    RESET_SECOND_PASSWORD("reset_second_password"),
    CHANNEL_ACTIVATION_REQUEST("channel_activation_request"),
    CHANNEL_ACTIVATION_SUCCESS("channel_successful_activation"),
    CHANNEL_ACTIVATION_FAILED("channel_failed_activation"),
    LOGIN_BLOCKED_MESSAGE("login_blocked_message"),
    REGISTER_BLOCKED_MESSAGE("register_blocked_message"),
    MB_ACTIVATION_CODE("mb_activation_code");

    private final String value;

    public static NotificationTemplate findByCode(String value) {
        return Arrays.stream(values())
                .filter(templateCode -> templateCode.getValue().equals(value))
                .findFirst()
                .orElse(null);
    }

}
