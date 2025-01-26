package ir.daneshrefah.scm.common.model.notification.constants;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationDataKey {

    BANK_NAME("bankName", true),
    TERMINAL_TITLE("terminalTitle", true),
    PERSON("person", false),
    PERSON_TITLE("person.Title", false),
    LOGIN_TIME("loginTime", true),
    OTP_CODE("otpCode", false),
    USER_NICKNAME("user.nickname", false),
    REASON("reason", false);

    private final String code;
    private final boolean builtIn;

    public static NotificationDataKey findByCode(String code) throws NullPointerException {
        for (NotificationDataKey value : values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }
}
