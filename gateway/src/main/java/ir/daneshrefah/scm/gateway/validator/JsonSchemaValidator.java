package ir.daneshrefah.scm.gateway.validator;

import ir.daneshrefah.scm.gateway.exception.ValidationException;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class JsonSchemaValidator extends AbstractValidator {
    @Override
    protected boolean support(Exchange exchange) {
        return false;
    }

    @Override
    protected void internalValidate(Exchange exchange) throws ValidationException {

    }
}
