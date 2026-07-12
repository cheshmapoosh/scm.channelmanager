package ir.daneshrefah.scm.core.integration.template.context;

import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class JwtClaimContextResolver implements ContextValueResolver {
    private static final String AUTHORITIES_KEY = "jwt.aut";

    @Override
    public boolean supports(String key) {
        return StringUtils.equalsIgnoreCase(key, AUTHORITIES_KEY);
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        return ExchangeAuthenticationContext.jwtBusinessContext(exchange).roles();
    }
}
