package ir.daneshrefah.scm.common.model.terminal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
@Getter
@RequiredArgsConstructor
public enum TerminalStatus {

    INACTIVE(0), ACTIVE(1);

    private final Integer code;

    public static TerminalStatus findByCode(int code) {
        return Arrays.stream(TerminalStatus.values())
                .filter(s -> s.code.equals(code))
                .findFirst()
                .orElse(null);
    }

}
