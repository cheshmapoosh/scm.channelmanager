package ir.daneshrefah.scm.common.data.model;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum TerminalType {

    IB("IB",210),
    MB("MB",22),
    SCM("SCM",211),
    HTP("HTP",2009),
    NIB("NIB",2012);

    private final String terminalCode;
    private final Integer legacyTerminalId;

    public TerminalType findByLegacyTerminalCode(Integer legacyTerminalId) {
        ValidationUtils.checkNull(legacyTerminalId, () -> new InvalidInputException("legacyTerminalId"));
        return Arrays.stream(TerminalType.values()).filter(terminalType -> terminalType.getLegacyTerminalId().equals(legacyTerminalId))
                .findFirst().orElse(null);
    }

    public TerminalType findByTerminalCode(String terminalCode) {
        ValidationUtils.checkBlankString(terminalCode, () -> new InvalidInputException("terminalCode"));
        return Arrays.stream(TerminalType.values()).filter(terminalType -> terminalType.getTerminalCode().equalsIgnoreCase(terminalCode))
                .findFirst().orElse(null);
    }
}




