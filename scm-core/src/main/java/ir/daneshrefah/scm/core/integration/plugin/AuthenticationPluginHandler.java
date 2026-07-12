package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.template.context.HeaderContextResolver;
import ir.daneshrefah.scm.uaa.starter.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.starter.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Map;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

@Component("jwtAuthPluginHandler")
@RequiredArgsConstructor
public class AuthenticationPluginHandler implements PluginHandler {

    private final ObjectProvider<AuthenticationClientTemplate> authenticationClientTemplateProvider;
    private final CoreObservationTraceSupport observationTraceSupport;
    private final HeaderContextResolver headerContextResolver;
    private final PersonProfileLoader profileLoader;
    private static final String AUTH_HEADER = "Authorization";
    private static final String JWT_HEADER = "jwt";

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {
        String authValue = exchange.getIn().getHeader(AUTH_HEADER, String.class);
        if (authValue == null || !authValue.startsWith("Bearer ")) {
            throw new AuthenticationRequiredException();
        }
        AuthenticationClientTemplate authenticationClientTemplate = authenticationClientTemplateProvider.getIfAvailable();
        if (authenticationClientTemplate == null) {
            throw new AuthenticationRequiredException();
        }
        ClientAuthenticationRequest authenticationRequest = convertToClientAuthenticationRequest(exchange);
        UserAuthentication authentication = authenticationClientTemplate.authenticateUserByAuthenticationRequest(authenticationRequest);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        profileLoader.preparePersonProfile(authentication);
        preserveValidatedJwt(exchange, authentication);
        enrichGatewayTrace(exchange);
    }

    private void preserveValidatedJwt(Exchange exchange, UserAuthentication authentication) {
        exchange.getIn().removeHeader(JWT_HEADER);
        UserAuthentication.AuthenticationDetail details = authentication == null ? null : authentication.getDetails();
        Object loginData = details == null ? null : details.getLoginData();
        if (loginData instanceof Jwt jwt) {
            exchange.getIn().setHeader(JWT_HEADER, jwt);
        }
    }

    private void enrichGatewayTrace(Exchange exchange) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        observationTraceSupport.enrichGatewayAuthentication(exchange, authentication);
    }


    private ClientAuthenticationRequest convertToClientAuthenticationRequest(Exchange exchange) {
        return ClientAuthenticationRequest.builder()
                .username((String) headerContextResolver.resolve(SCM_PARAMETER_USERNAME, exchange))
                .terminalCode((String) headerContextResolver.resolve(SCM_PARAMETER_TERMINAL, exchange))
                .clientId((String) headerContextResolver.resolve(SCM_PARAMETER_CLIENT_ID, exchange))
                .tokenType(AuthenticationUtils.extractTokenType(
                        (String) headerContextResolver.resolve(SCM_PARAMETER_AUTHORIZATION, exchange),
                        (String) headerContextResolver.resolve(SCM_PARAMETER_USERNAME, exchange),
                        (String) headerContextResolver.resolve(SCM_PARAMETER_CREDENTIAL, exchange)))
                .authenticationValue(AuthenticationUtils.extractAuthenticationValue((String) headerContextResolver.resolve(SCM_PARAMETER_AUTHORIZATION, exchange)))
                .accessParameter((String) headerContextResolver.resolve(SCM_PARAMETER_ACCESS_PARAMETER, exchange))
                .build();
    }
}
