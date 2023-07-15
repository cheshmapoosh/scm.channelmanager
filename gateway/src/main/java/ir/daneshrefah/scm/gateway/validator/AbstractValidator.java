package ir.daneshrefah.scm.gateway.validator;

import ir.daneshrefah.scm.gateway.exception.ValidationException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

public abstract class AbstractValidator  {

    public void validate(Exchange exchange) throws Exception {
        boolean isSupported = support(exchange);
        if (isSupported)
            internalValidate(exchange);
    }

    protected abstract boolean support (Exchange exchange);

    protected abstract void internalValidate(Exchange exchange) throws ValidationException;

}
