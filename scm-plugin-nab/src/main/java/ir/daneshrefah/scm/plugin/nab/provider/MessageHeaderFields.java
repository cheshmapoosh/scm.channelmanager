package ir.daneshrefah.scm.plugin.nab.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-28
 */
@Getter
@RequiredArgsConstructor
public enum MessageHeaderFields {

    COMMAND(2),
    SERVICE(2),
    DATE_TIME(14),
    CM_USER_ID(10),
    CM_PASSWORD(10),
    RQUID(16);

    private final int length;

}
