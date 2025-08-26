package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthorizationPluginHandler implements PluginHandler {
    private static final String JWT_INSTANCE = "jwt";

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        System.out.println("salm");
        properties.get("token");
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        Jwt jwt = (Jwt) exchange.getIn().getHeader(JWT_INSTANCE);
    }
}
