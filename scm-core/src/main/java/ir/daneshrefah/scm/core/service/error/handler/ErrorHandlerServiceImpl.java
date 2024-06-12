package ir.daneshrefah.scm.core.service.error.handler;

import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.error.management.ExceptionResolverHelper;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-29
 */
@Service
public class ErrorHandlerServiceImpl extends ErrorHandlerService {


    @Override
    public Message resolveMessageByValidationMessage(Message message, Set<ValidationMessage> errors) {
        for (Iterator<ValidationMessage> iterator = errors.iterator(); iterator.hasNext(); ) {
            ValidationMessage validationMessage = iterator.next();
            Error error = new Error(validationMessage.getProperty(),
                    validationMessage.getCode(), validationMessage.getMessage(), null);
            message.addError(error, MessageStatus.SC_ERROR_VALIDATION);
        }
        return message;
    }

    @Override
    public Message resolveMessageByException(Message message, Exception exception) {
        if (exception instanceof InvocationTargetException && null != exception.getCause()) {
            return resolveMessageByException(message, (Exception) exception.getCause());
        }
        if (exception.getClass().isAssignableFrom(RuntimeException.class) && null != exception.getCause()) {
            return resolveMessageByException(message, (Exception) exception.getCause());
        }

        Error resolve = ExceptionResolverHelper.getInstance().resolve(exception, AccessibleLocale.EN_US.getLocale());
        message.addError(resolve);
        return message;
    }



}
