package ir.daneshrefah.scm.uaa.common.type;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum AuthenticationMethod {
    STATIC_PASSWORD("SPD", "resource.uaa.authentication-method.spd"),
    OTP("OTP", "resource.uaa.authentication-method.otp"),
    PUBLIC_KEY("PKI", "resource.uaa.authentication-method.pki"),
    PIN("PIN", "resource.uaa.authentication-method.pin"),
    PATTERN("PTN", "resource.uaa.authentication-method.ptn"),
    SMS("SMS", "resource.uaa.authentication-method.sms");

    AuthenticationMethod(String code, String title) {
        this.code = code;
        this.title = title;
    }

    private final String code;
    private final String title;

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public static AuthenticationMethod findByCode(String code) {
        AuthenticationMethod[] attrs = AuthenticationMethod.values();
        for (AuthenticationMethod attr : attrs) {
            if (attr.getCode().equals(code)) {
                return attr;
            }
        }
        return null;
    }

}
