package ir.daneshrefah.scm.common.model.terminal;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
public enum Protocol {

    REST(1), JMS(2), RMI(3), JAVA(4);

    private Integer code;

    Protocol(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static Protocol findByCode(Integer code) {
        for (Protocol enumValue : Protocol.values()) {
            if (enumValue.getCode().equals(code)) {
                return enumValue;
            }
        }
        return null;
    }
}
