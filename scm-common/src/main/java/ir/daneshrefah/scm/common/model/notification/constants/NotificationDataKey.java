package ir.daneshrefah.scm.common.model.notification.constants;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationDataKey {

    BANK_NAME("bankName", true, null),
    TERMINAL_TITLE("terminalTitle", true, null),
    CHANNEL_TITLE("channelTitle", false, null),
    PERSON("person", false, null),
    PERSON_TITLE("personTitle", false, null),
    LOGIN_TIME("loginTime", true, null),
    TIME("time", true, null),
    PASSWORD("password", false, null),
    OTP_CODE("otpCode", false, null),
    USER_NICKNAME("userNickname", false, null),
    BLOCKED_TIME("blockedTime", false, null),
    MOBILE("mobile", false, null),
    TRAILS("trials", false, null),
    CODE("code", false, null),
    HASH_CODE("hashCode", false, null),
    REASON("reason", false, null),
    AMOUNT("amount", true, Number.class),
    RECEIVER("receiver", true, String.class);

    private final String code;
    private final boolean builtIn;
    private final Class classType;

    public static NotificationDataKey findByCode(String code) throws NullPointerException {
        for (NotificationDataKey value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
