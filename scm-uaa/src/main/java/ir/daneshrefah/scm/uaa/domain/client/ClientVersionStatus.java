package ir.daneshrefah.scm.uaa.domain.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-31
 */
@Getter
@RequiredArgsConstructor
public enum ClientVersionStatus {

    VALID(1),
    INVALID(2),
    NOT_RECOMMENDED(3);

    private final int code;

}
