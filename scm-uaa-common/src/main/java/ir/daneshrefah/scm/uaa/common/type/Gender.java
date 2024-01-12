package ir.daneshrefah.scm.uaa.common.type;

import lombok.Getter;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
public enum Gender {

    MALE(1),
    FEMALE(2),
    UNKNOWN(0);

    Gender(Integer code) {
        this.code = code;
    }

    private final Integer code;

    public static Gender findByCode(Integer code) {
        return Arrays.stream(Gender.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}
