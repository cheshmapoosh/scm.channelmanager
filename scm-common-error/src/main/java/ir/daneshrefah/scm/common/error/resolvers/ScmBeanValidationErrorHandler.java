package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.error.bean.validation.ScmBeanValidationException;
import ir.daneshrefah.scm.common.error.management.ErrorHandler;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ScmBeanValidationErrorHandler implements ErrorHandler {
    @Override
    public boolean support(Exception exception) {
        return exception instanceof ScmBeanValidationException;
    }

    @Override
    public ScmFault handle(Exception exception, Locale locale) {
        ScmBeanValidationException beanValidationException = (ScmBeanValidationException) exception;
        List<Error> errors = beanValidationException.getErrors();
        ScmFault scmFault = new ScmFault();
        scmFault.setErrors(errors);
        scmFault.setStatus(MessageStatus.SC_ERROR_VALIDATION);
        return scmFault;
    }


}
