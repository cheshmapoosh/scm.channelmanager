package ir.daneshrefah.scm.gateway.validator;

import ir.daneshrefah.scm.gateway.exception.ValidationException;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DynamicValidator extends AbstractValidator {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    protected boolean support(Exchange exchange) {
        return true;
    }

    @Override
    protected void internalValidate(Exchange exchange) throws ValidationException {
        Map<String, AbstractValidator> beansOfType = applicationContext.getBeansOfType(AbstractValidator.class);
        for (AbstractValidator bean : beansOfType.values()) {
            // Use the bean as needed
        }
    }

}
