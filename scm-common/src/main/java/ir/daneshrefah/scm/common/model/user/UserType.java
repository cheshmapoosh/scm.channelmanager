package ir.daneshrefah.scm.common.model.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-06-18
 */
@Getter
@RequiredArgsConstructor
public enum UserType {

    CM_REGULAR(1),
    CM_EMPLOYEE(2),
    CM_CLIENT(3),
    SMS_VERIFIED(4),
    SHAHKAR_VERIFIED(5);

    private final int code;

    public static UserType findByCode(int code) {
        return Arrays.stream(UserType.values())
                .filter(s -> s.code == code)
                .findFirst()
                .orElse(null);
    }

}
