package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_GLOBAL;

public class DuplicatedRecordFoundException extends AbstractValidationException {

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_GLOBAL;
    }

    private final String source;
    public DuplicatedRecordFoundException(String source) {
        super(  source + "' is duplicated.", null);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

}
