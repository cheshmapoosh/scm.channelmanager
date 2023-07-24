package ir.daneshrefah.scm.plugin.api.model.service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum ServiceImplementationType {

    DIRECT(1), JAVA(2), BPMN(3);

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
