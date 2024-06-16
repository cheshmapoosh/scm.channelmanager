package ir.daneshrefah.scm.common.model.user;

import lombok.Getter;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
public enum AuthenticationMethod {
    STATIC_PASSWORD("SPD", 1),
    OTP("OTP", 2),
    PUBLIC_KEY("PKI", 3),
    PIN("PIN", 4),
    PATTERN("PTN", 5),
    SMS("SMS", 6);

    AuthenticationMethod(String code, Integer dbRef) {
        this.code = code;
        this.dbRef = dbRef;
    }

    private final String code;
    private final Integer dbRef;

    public static AuthenticationMethod findByCode(String code) {
        return Arrays.stream(values())
                .filter(m -> m.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }

    public static AuthenticationMethod findByDbRef(Integer dbref) {
        return Arrays.stream(values())
                .filter(m -> m.dbRef.equals(dbref))
                .findFirst()
                .orElse(null);
    }

    public static AuthenticationMethod findByName(String authenticationMethod) {
        AuthenticationMethod[] attrs = AuthenticationMethod.values();
        for (AuthenticationMethod attr : attrs) {
            if (attr.name().equals(authenticationMethod.toUpperCase())) {
                return attr;
            }
        }
        return null;
    }

}
