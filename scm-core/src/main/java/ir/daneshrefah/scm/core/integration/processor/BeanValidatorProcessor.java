package ir.daneshrefah.scm.core.integration.processor;

import ir.daneshrefah.scm.common.error.bean.validation.ScmBeanValidator;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

@Component("beanValidator")
public class BeanValidatorProcessor implements Processor {
    @Override
    public void process(Exchange exchange) throws Exception {
        try (ScmBeanValidator scmBeanValidator = new ScmBeanValidator()) {
            scmBeanValidator.validateBean(exchange.getIn().getBody());
        }
    }
}
