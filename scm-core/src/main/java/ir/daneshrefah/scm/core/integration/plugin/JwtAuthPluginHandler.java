package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.exception.ScmException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthPluginHandler implements PluginHandler {

    private String authHeader = "Authorization";
    private final JwtDecoder jwtDecoder;

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        if (properties.containsKey("authHeader")) {
            authHeader = (String) properties.get("authHeader");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        String authValue = exchange.getIn().getHeader(authHeader, String.class);
        if (authValue == null || !authValue.startsWith("Bearer ")) {
            throw new ScmException("SCM.100001", "Auth missing");
        }

        String token = authValue.substring("Bearer ".length());
        Jwt jwt;

        try {
            jwt = jwtDecoder.decode(token);
        } catch (JwtException e) {
            throw new ScmException("SCM.100002", "Jwt invalid", e);
        }

        // Put the decoded JWT claims in the exchange property for downstream plugins
        exchange.setProperty("jwt", jwt);
    }
}