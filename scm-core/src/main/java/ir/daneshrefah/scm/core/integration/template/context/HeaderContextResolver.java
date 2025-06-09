package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class HeaderContextResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return StringUtils.startsWithIgnoreCase(key, "header.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String headerName = StringUtils.removeStartIgnoreCase(key, "header.");
        return exchange.getIn().getHeader(headerName);
    }
}