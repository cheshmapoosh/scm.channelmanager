package ir.daneshrefah.scm.common.constant;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum TerminalType {

    IB("IB", 210),
    MB("MB", 211),
    SCM("SCM", 1304),
    HTP("HTP", 2009),
    NIB("NIB", 2012),
    CMC("CMC", 22),
    CIB("CIB", 1144);

    private final String terminalCode;
    private final Integer legacyTerminalId;

    public static Optional<TerminalType> fromCode(String code) {
        return Arrays.stream(values())
                .filter(terminalCode -> terminalCode.name().equals(code))
                .findFirst();
    }

    public static TerminalType findByLegacyTerminalCode(Integer legacyTerminalId) {
        if (Objects.isNull(legacyTerminalId)) {
            throw new InvalidInputException("legacyTerminalId");
        }
        return Arrays.stream(TerminalType.values()).filter(terminalType -> terminalType.getLegacyTerminalId().equals(legacyTerminalId))
                .findFirst().orElse(null);
    }

    public static TerminalType findByTerminalCode(String terminalCode) {
        if (Objects.isNull(terminalCode)) {
            throw new InvalidInputException("terminalCode");
        }
        return Arrays.stream(TerminalType.values()).filter(terminalType -> terminalType.getTerminalCode().equalsIgnoreCase(terminalCode))
                .findFirst().orElse(null);
    }
}




