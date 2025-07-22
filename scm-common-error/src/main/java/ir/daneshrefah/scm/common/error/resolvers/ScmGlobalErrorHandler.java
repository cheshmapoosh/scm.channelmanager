package ir.daneshrefah.scm.common.error.resolvers;

import ir.daneshrefah.scm.common.error.management.ErrorHandler;
import ir.daneshrefah.scm.common.error.management.ExceptionResolverHelper;
import ir.daneshrefah.scm.common.model.error.Error;
import ir.daneshrefah.scm.common.model.error.ScmFault;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class ScmGlobalErrorHandler implements ErrorHandler {
    @Override
    public boolean support(Exception exception) {
        return true;
    }

    @Override
    public ScmFault handle(Exception exception, Locale locale) {
        //TODO TEMPORARY : FOR SAVING CURRENT EXCEPTIONS MECHANISING
        List<Error> errors = ExceptionResolverHelper.getInstance().resolve(exception, locale);
        ScmFault scmFault = new ScmFault();
        scmFault.setErrors(errors);
        scmFault.setStatus(errors.get(0).getStatus());
        return scmFault;
    }
}
