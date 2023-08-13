package ir.daneshrefah.scm.common.model.service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum ServiceImplementationType {

    EXTERNAL(1), JAVA(2), COMPOSITION(3), BPMN(4), PARENT(5);

    private Integer code;

    ServiceImplementationType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static ServiceImplementationType findByCode(Integer code) {
        for (ServiceImplementationType enumValue : ServiceImplementationType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
