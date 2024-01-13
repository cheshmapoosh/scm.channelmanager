package ir.daneshrefah.scm.common.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-13
 */
@RequiredArgsConstructor
@Getter
public enum ConditionType {
    RATE(1), WITHDRAW(2), AUTHORITY(3);

    private final Integer code;

    public static ConditionType findByCode(Integer code) {
        return Arrays.stream(ConditionType.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }
}
