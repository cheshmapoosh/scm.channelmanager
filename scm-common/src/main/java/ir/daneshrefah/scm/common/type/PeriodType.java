package ir.daneshrefah.scm.common.type;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-12
 */
@RequiredArgsConstructor
@Getter
public enum PeriodType {
    REQUEST(1), SECOND(2), MINUTE(3), HOUR(4), DAY(5), WEEK(6),
    MONTH(7), YEAR(8);

    private final int code;

    public static PeriodType findByCode(Integer code) {
        if (Objects.isNull(code)) {
            return null;
        }
        for (PeriodType enumValue : PeriodType.values()) {
            if (enumValue.getCode() == code) {
                return enumValue;
            }
        }
        return null;
    }
}
