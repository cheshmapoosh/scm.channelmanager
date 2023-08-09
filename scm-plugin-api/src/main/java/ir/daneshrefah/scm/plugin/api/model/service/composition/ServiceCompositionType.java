package ir.daneshrefah.scm.plugin.api.model.service.composition;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-08
 */
public enum ServiceCompositionType {

    SAGA(1), FAILOVER(3), ROUND_ROBIN(4);

    ServiceCompositionType(Integer code) {
        this.code = code;
    }

    private Integer code;

    public Integer getCode() {
        return code;
    }

    public static ServiceCompositionType findByCode(Integer code) {
        for (ServiceCompositionType enumValue : ServiceCompositionType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }

}