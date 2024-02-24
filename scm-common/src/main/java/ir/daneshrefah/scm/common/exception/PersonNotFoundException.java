package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PERSON_NOT_FOUND;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
public class PersonNotFoundException extends BasePersonException implements ErrorCodeAwareException {


    public PersonNotFoundException(String message) {
        super(message);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_PERSON_NOT_FOUND;
    }

    @Override
    public String getSource() {
        return null;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_VALIDATION;
    }

}
