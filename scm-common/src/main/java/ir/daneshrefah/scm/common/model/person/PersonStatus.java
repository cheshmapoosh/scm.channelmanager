package ir.daneshrefah.scm.common.model.person;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-16
 */
@Getter
@RequiredArgsConstructor
public enum PersonStatus {

    INACTIVE(0),
    ACTIVE(1),
    DELETED(2);

    private final int code;

    public static PersonStatus findByCode(int code) {
        return Arrays.stream(PersonStatus.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }

}
