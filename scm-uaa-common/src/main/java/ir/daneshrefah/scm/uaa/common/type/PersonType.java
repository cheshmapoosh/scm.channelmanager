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
public enum PersonType {

    INDIVIDUAL_CUSTOMER("1"), //real
    EMPLOYEE("2"),
    CORPORATE_CUSTOMER("3"), //legal;
    CLIENT("4");

    PersonType(String code) {
        this.code = code;
    }

    private final String code;
    public static PersonType findByCode(String code) {
        return Arrays.stream(PersonType.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}
