package ir.daneshrefah.scm.core.service.error.handler;

import com.networknt.schema.ValidationMessage;
import ir.daneshrefah.scm.common.constant.AccessibleLocale;
import ir.daneshrefah.scm.common.error.management.ExceptionResolverHelper;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.plugin.api.integration.ErrorHandlerService;
import ir.daneshrefah.scm.utils.MessageInputContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
        for (ValidationMessage validationMessage : errors) {
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
        Locale locale = findRequestLocale();
        List<Error> resolves = ExceptionResolverHelper.getInstance().resolve(exception,message, locale);
        resolves.forEach(message::addError);
        return message;
    }

    private Locale findRequestLocale() {
        Locale locale = AccessibleLocale.EN_US.getLocale();
        try {
            String acceptLanguageHeader = MessageInputContext.getCurrentContext().getHeader("Accept-Language");
            if (Objects.nonNull(acceptLanguageHeader)) {
                String[] acceptLanguages = acceptLanguageHeader.split(",");
                for (String acceptLanguage : acceptLanguages) {
                    AccessibleLocale foundLocale = AccessibleLocale.findByLocale(acceptLanguage).orElse(null);
                    if (Objects.nonNull(foundLocale)) {
                        locale = foundLocale.getLocale();
                        break;
                    }
                }
            }
            return locale;
        } catch (Exception e) {
            return locale;
        }
    }


}
