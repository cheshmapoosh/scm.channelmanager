package ir.daneshrefah.scm.process.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_TASK_NOT_FOUND_EXCEPTION;

public class TaskNotFoundException extends AbstractProcessException {

    public TaskNotFoundException(String source, String message) {
        super(source, message);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_TASK_NOT_FOUND_EXCEPTION;
    }
}
