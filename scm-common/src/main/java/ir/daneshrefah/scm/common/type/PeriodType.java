package ir.daneshrefah.scm.common.type;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-12
 */
public enum PeriodType {
    REQUEST(1),SECOND(2),MINUTE(3),HOUR(4),DAY(5), WEEK(6), MONTH(7), YEAR(8);

    PeriodType(int code) {
        this.code = code;
    }

    private int code;

    public int getCode() {
        return code;
    }

    public static PeriodType findByCode(Integer code) {
        for (PeriodType enumValue : PeriodType.values()) {
            if (enumValue.getCode() == code) {
                return enumValue;
            }
        }
        return null;
    }
}
