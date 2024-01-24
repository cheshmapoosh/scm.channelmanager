package ir.daneshrefah.scm.plugin.api.integration;

import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.model.message.Message;

import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-24
 */
public interface ErrorHandlerService {

    public Message resolveMessageByValidationMessage(Message message, Set<ValidationMessage> errors);

    public Message resolveMessageByException(Message message, Exception exception);

}
