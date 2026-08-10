package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.integration.template.context.HeaderContextResolver;
import ir.daneshrefah.scm.uaa.client.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

@Component("jwtAuthPluginHandler")
@RequiredArgsConstructor
public class AuthenticationPluginHandler implements PluginHandler {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_COOKIE = "Authorization";
    private static final String COOKIE_HEADER = "Cookie";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthenticationClientTemplate authenticationClientTemplate;
    private final HeaderContextResolver headerContextResolver;
    private final PersonProfileLoader profileLoader;
    private final JwtDecoder jwtDecoder;

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) throws Exception {

        String authorization = resolveAuthorization(exchange);

        if (StringUtils.isBlank(authorization) || !StringUtils.startsWithIgnoreCase(authorization, BEARER_PREFIX)) {
            throw new AuthenticationRequiredException();
        }


        exchange.getIn().setHeader(AUTHORIZATION_HEADER, authorization);

        String token = authorization.substring(BEARER_PREFIX.length()).trim();

        if (StringUtils.isBlank(token)) {
            throw new AuthenticationRequiredException();
        }

        try {
            ClientAuthenticationRequest authenticationRequest = convertToClientAuthenticationRequest(exchange);

            UserAuthentication authentication = authenticationClientTemplate.authenticateUserByAuthenticationRequest(authenticationRequest);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            profileLoader.preparePersonProfile(authentication);

            exchange.getIn().setHeader("jwt", jwtDecoder.decode(token));
        } catch (JwtException exception) {
            throw new AuthenticationRequiredException();
        }
    }

    private String resolveAuthorization(Exchange exchange) {
        String authorizationHeader = exchange.getIn().getHeader(AUTHORIZATION_HEADER, String.class);


        if (StringUtils.startsWithIgnoreCase(authorizationHeader, BEARER_PREFIX)) {
            return authorizationHeader;
        }


        return extractAuthorizationCookie(exchange).map(token -> BEARER_PREFIX + token).orElse(null);
    }

    private Optional<String> extractAuthorizationCookie(Exchange exchange) {

        String cookieHeader = exchange.getIn().getHeader(COOKIE_HEADER, String.class);

        if (StringUtils.isBlank(cookieHeader)) {
            return Optional.empty();
        }

        return Arrays.stream(cookieHeader.split(";")).map(String::trim).map(this::parseCookie).filter(Optional::isPresent).map(Optional::get).findFirst();
    }

    private Optional<String> parseCookie(String cookiePart) {
        int separatorIndex = cookiePart.indexOf('=');

        if (separatorIndex <= 0) {
            return Optional.empty();
        }

        String name = cookiePart.substring(0, separatorIndex).trim();

        if (!AUTHORIZATION_COOKIE.equals(name)) {
            return Optional.empty();
        }

        String value = cookiePart.substring(separatorIndex + 1).trim();

        return StringUtils.isBlank(value) ? Optional.empty() : Optional.of(value);
    }

    private ClientAuthenticationRequest convertToClientAuthenticationRequest(Exchange exchange) {

        String authorization = (String) headerContextResolver.resolve(SCM_PARAMETER_AUTHORIZATION, exchange);

        return ClientAuthenticationRequest.builder().username((String) headerContextResolver.resolve(SCM_PARAMETER_USERNAME, exchange)).terminalCode((String) headerContextResolver.resolve(SCM_PARAMETER_TERMINAL, exchange)).clientId((String) headerContextResolver.resolve(SCM_PARAMETER_CLIENT_ID, exchange)).tokenType(AuthenticationUtils.extractTokenType(authorization, (String) headerContextResolver.resolve(SCM_PARAMETER_USERNAME, exchange), (String) headerContextResolver.resolve(SCM_PARAMETER_CREDENTIAL, exchange))).authenticationValue(AuthenticationUtils.extractAuthenticationValue(authorization)).accessParameter((String) headerContextResolver.resolve(SCM_PARAMETER_ACCESS_PARAMETER, exchange)).build();
    }
}