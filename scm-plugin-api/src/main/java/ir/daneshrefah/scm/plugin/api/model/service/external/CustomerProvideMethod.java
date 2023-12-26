package ir.daneshrefah.scm.plugin.api.model.service.external;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-26
 */
public enum CustomerProvideMethod {

    NO_CUSTOMER(1), PERSON_PROPERTY(2), FIX(3), MANUAL(4), SERVICE(5);

    private Integer code;

    CustomerProvideMethod(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static CustomerProvideMethod findByCode(Integer code) {
        for (CustomerProvideMethod enumValue : CustomerProvideMethod.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

}
