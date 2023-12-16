package ir.daneshrefah.scm.uaa.common.type;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum AuthenticationMethod {
    STATIC_PASSWORD("SPD", "resource.uaa.authentication-method.spd",1L),
    OTP("OTP", "resource.uaa.authentication-method.otp",2L),
    PUBLIC_KEY("PKI", "resource.uaa.authentication-method.pki",3L),
    PIN("PIN", "resource.uaa.authentication-method.pin",4L),
    PATTERN("PTN", "resource.uaa.authentication-method.ptn",5L),
    SMS("SMS", "resource.uaa.authentication-method.sms",6L);

    AuthenticationMethod(String code, String title, Long id) {
        this.code = code;
        this.title = title;
        this.id = id;
    }

    private final String code;
    private final String title;
    private final Long id;

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public Long getId() {
        return id;
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

    public static AuthenticationMethod findById(Long id) {
        AuthenticationMethod[] attrs = AuthenticationMethod.values();
        for (AuthenticationMethod attr : attrs) {
            if (attr.getId().equals(id)) {
                return attr;
            }
        }
        return null;
    }

}
