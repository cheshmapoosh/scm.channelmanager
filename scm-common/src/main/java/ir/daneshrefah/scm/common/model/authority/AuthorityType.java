package ir.daneshrefah.scm.common.model.authority;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum AuthorityType {

    TERMINAL_SERVICE_ACCESS(1), TERMINAL_WITHDRAW(2), USER_SERVICE_ACCESS(3),
    USER_WITHDRAW(4);

    private Integer code;

    AuthorityType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static AuthorityType findByCode(Integer code) {
        for (AuthorityType enumValue : AuthorityType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
