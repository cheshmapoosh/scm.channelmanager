package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class JwtClaimContextResolver implements ContextValueResolver {
    @Override
    public boolean supports(String key) {
        return StringUtils.startsWithIgnoreCase(key, "jwt.");
    }

    @Override
    public Object resolve(String key, Exchange exchange) {
        String claim = StringUtils.removeStartIgnoreCase(key, "jwt.");
        Jwt jwt = (Jwt) exchange.getIn().getHeader("jwt"); // or your JWT injection logic
        return jwt.getClaim(claim);
    }
}