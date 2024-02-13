package ir.daneshrefah.scm.common.model.person;

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
public enum MaritalStatus {

    MARRIED("M"),
    SINGLE("S");

    MaritalStatus(String code) {
        this.code = code;
    }

    private final String code;


    public static MaritalStatus findByCode(String code) {
        return Arrays.stream(MaritalStatus.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}
