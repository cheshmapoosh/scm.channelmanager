package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionDynamicMessage;
import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-13
 */
public class PersonNotFoundException extends BasePersonException  {

    private final ExceptionDynamicMessage dynamicMessage;
    public PersonNotFoundException(String message, ExceptionDynamicMessage dynamicMessage) {
        super(message);
        this.dynamicMessage = dynamicMessage;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .dynamicMessage(dynamicMessage)
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }


}
