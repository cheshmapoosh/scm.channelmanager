package ir.daneshrefah.scm.plugin.api.model.service.composition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
public enum ServiceRelationType {

    COMPOSITION(1), CHILD(2);

    ServiceRelationType(Integer code) {
        this.code = code;
    }

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public static ServiceRelationType findByCode(Integer code) {
        for (ServiceRelationType enumValue : ServiceRelationType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

}