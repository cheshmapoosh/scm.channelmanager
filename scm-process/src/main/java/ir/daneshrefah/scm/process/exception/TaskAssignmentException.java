package ir.daneshrefah.scm.process.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_TASK_ASSIGMENT_EXCEPTION;

public class TaskAssignmentException extends AbstractProcessException {

    public TaskAssignmentException(String source, String message) {
        super(source, message);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_TASK_ASSIGMENT_EXCEPTION;
    }
}
