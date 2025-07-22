package ir.daneshrefah.scm.common.error;

import ir.daneshrefah.scm.common.error.management.ErrorHandler;
import ir.daneshrefah.scm.common.error.management.ErrorHandlerChain;
import ir.daneshrefah.scm.common.error.resolvers.ScmBeanValidationErrorHandler;
import ir.daneshrefah.scm.common.error.resolvers.ScmGlobalErrorHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ErrorHandlerChainImpl extends ErrorHandlerChain {


    public ErrorHandlerChainImpl(List<ErrorHandler> errorHandlers) {
        super(errorHandlers);
    }

    @Override
    public List<Class<? extends ErrorHandler>> errorHandlerChain() {
        List<Class<? extends ErrorHandler>> chain = new ArrayList<>();
        chain.add(ScmBeanValidationErrorHandler.class);
        chain.add(ScmGlobalErrorHandler.class);
        return chain;
    }
}
