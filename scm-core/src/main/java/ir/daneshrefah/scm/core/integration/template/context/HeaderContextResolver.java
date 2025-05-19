package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class HeaderContextResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return key.startsWith("header.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String headerName = key.substring("header.".length());
        return exchange.getIn().getHeader(headerName);
    }
}