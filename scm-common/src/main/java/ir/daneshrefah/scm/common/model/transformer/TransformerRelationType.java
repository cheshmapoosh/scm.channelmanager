package ir.daneshrefah.scm.common.model.transformer;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum TransformerRelationType {

    SERVICE_REQUEST(1),
    SERVICE_RESPONSE(2),
    SERVICE_RELATION_REQUEST(3),
    SERVICE_RELATION_RESPONSE(4),
    SERVICE_RELATION_COMMIT_REQUEST(5),
    SERVICE_RELATION_COMMIT_RESPONSE(6),
    SERVICE_RELATION_REVERSE_REQUEST(7),
    SERVICE_RELATION_REVERSE_RESPONSE(8),
    TERMINAL_REQUEST(9),
    TERMINAL_RESPONSE(10);

    private Integer code;

    TransformerRelationType(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static TransformerRelationType findByCode(Integer code) {
        for (TransformerRelationType enumValue : TransformerRelationType.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
