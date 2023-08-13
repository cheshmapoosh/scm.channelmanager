package ir.daneshrefah.scm.common.type;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-12
 */
public enum DurationType {
    DAY(1), WEEK(2), MONTH(3), YEAR(4), ACTION(5);

    DurationType(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static DurationType findByCode(Integer code) {
        for (DurationType enumValue : DurationType.values()) {
            if (enumValue.getCode() == code) {
                return enumValue;
            }
        }
        return null;
    }
}
