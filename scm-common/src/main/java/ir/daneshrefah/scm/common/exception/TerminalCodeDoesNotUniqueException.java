package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.exception.AbstractValidationException;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_GLOBAL;

public class TerminalCodeDoesNotUniqueException extends AbstractValidationException {

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_GLOBAL;
    }

    private final String terminalCode;
    public TerminalCodeDoesNotUniqueException(String terminalCode) {
        super( "'terminal code '" + terminalCode + "' is duplicated.", null);
        this.terminalCode = terminalCode;
    }

    @Override
    public String getSource() {
        return terminalCode;
    }

}
