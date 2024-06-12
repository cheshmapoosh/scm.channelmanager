package ir.daneshrefah.scm.process.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_PROCESS_FILE_NOT_FOUND_EXCEPTION;

public class JsonSchemaNullException extends AbstractProcessException{

    public JsonSchemaNullException(String source, String message) {
        super(source, message);
    }

    @Override
    public int getErrorCode() {
        return 1;
    }//TODO change this error code
}
