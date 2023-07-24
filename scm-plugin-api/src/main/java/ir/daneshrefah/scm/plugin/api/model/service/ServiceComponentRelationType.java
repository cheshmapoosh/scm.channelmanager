package ir.daneshrefah.scm.plugin.api.model.service;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-24
 */
public enum ServiceComponentRelationType {

    NONE(1), DYNAMIC(2), JAVA(3);

    ServiceComponentRelationType(Integer code) {
        this.code = code;
    }

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public static ServiceComponentRelationType findByCode(Integer code) {
        for (ServiceComponentRelationType enumValue : ServiceComponentRelationType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

}
