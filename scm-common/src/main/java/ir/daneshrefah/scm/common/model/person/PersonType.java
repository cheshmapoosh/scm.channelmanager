package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-19
 */
@Getter
@RequiredArgsConstructor
public enum PersonType {

    UNKNOWN(0),
    REAL(1),
    EMPLOYEE(2),
    CORPORATE(3),
    GOVERNANCE(4),
    BANK(5),
    TAMIN(6),
    /**
     * this person type is used for clients that defined in {@link ClientPerson}
     * */
    CLIENT(7);

    private final int code;

    public static PersonType findByCode(int code) {
        return Arrays.stream(PersonType.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }

}
