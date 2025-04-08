package ir.daneshrefah.scm.common.constant;

import java.util.Arrays;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
public enum TerminalCodes {

    IB,
    CMC, // code of branch terminal
    NIB,
    CIB;

    public static Optional<TerminalCodes> fromString(String code) {
        return Arrays.stream(values())
                .filter(terminalCode -> terminalCode.name().equals(code))
                .findFirst();
    }
}
