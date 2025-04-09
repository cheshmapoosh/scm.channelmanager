package ir.daneshrefah.scm.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@RequiredArgsConstructor
@Getter
public enum TerminalCodes {

    IB(210),
    CMC(22), // code of branch terminal
    NIB(2012),
    CIB(1144),
    SCM(1304);

    private final Integer legacyTerminalId;

    public static Optional<TerminalCodes> fromString(String code) {
        return Arrays.stream(values())
                .filter(terminalCode -> terminalCode.name().equals(code))
                .findFirst();
    }
}
