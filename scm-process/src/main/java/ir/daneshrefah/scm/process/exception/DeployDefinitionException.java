package ir.daneshrefah.scm.process.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_PROCESS_FILE_NOT_FOUND_EXCEPTION;

public class DeployDefinitionException extends AbstractProcessException{

    public DeployDefinitionException(String source, String message) {
        super(source, message);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_PROCESS_FILE_NOT_FOUND_EXCEPTION;
    }
}
