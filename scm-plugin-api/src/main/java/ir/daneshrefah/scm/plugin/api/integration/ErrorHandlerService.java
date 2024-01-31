package ir.daneshrefah.scm.plugin.api.integration;

import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.Message;

import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
public abstract class ErrorHandlerService {

    private static ErrorHandlerService INSTANCE;

    public ErrorHandlerService() {
        INSTANCE = this;
    }

    public static ErrorHandlerService getInstance() {
        return INSTANCE;
    }

    public abstract Message resolveMessageByValidationMessage(Message message, Set<ValidationMessage> errors);

    public abstract Message resolveMessageByException(Message message, Exception exception);

    public abstract BaseException resolveExceptionByError(Message message);

}
