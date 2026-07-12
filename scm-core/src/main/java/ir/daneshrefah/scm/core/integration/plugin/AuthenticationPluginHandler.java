package ir.daneshrefah.scm.core.integration.plugin;

import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.integration.observability.CamelSecurityTraceEventRecorder;
import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import ir.daneshrefah.scm.core.integration.security.ValidatedJwtBusinessContext;
import ir.daneshrefah.scm.core.integration.template.context.HeaderContextResolver;
import ir.daneshrefah.scm.uaa.starter.core.AuthenticationClientTemplate;
import ir.daneshrefah.scm.uaa.starter.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.starter.security.event.ScmSecurityEventType;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.model.RouteDefinition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ir.daneshrefah.scm.utils.constant.Constants.*;

@Component("jwtAuthPluginHandler")
@RequiredArgsConstructor
public class AuthenticationPluginHandler implements PluginHandler {

    private final ObjectProvider<AuthenticationClientTemplate> authenticationClientTemplateProvider;
    private final CoreObservationTraceSupport observationTraceSupport;
    private final ObjectProvider<CamelSecurityTraceEventRecorder> securityTraceEventRecorders;
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
        removeJwtHeader(exchange);
        ExchangeAuthenticationContext.clear(exchange);
        String authValue = exchange.getIn().getHeader(AUTH_HEADER, String.class);
        if (authValue == null || !authValue.startsWith("Bearer ")) {
            recordSecurityEvent(exchange, ScmSecurityEventType.TOKEN_MISSING, Map.of());
            throw new AuthenticationRequiredException();
        }
        AuthenticationClientTemplate authenticationClientTemplate = authenticationClientTemplateProvider.getIfAvailable();
        if (authenticationClientTemplate == null) {
            recordSecurityEvent(exchange, ScmSecurityEventType.AUTHENTICATION_FAILURE, Map.of());
            throw new AuthenticationRequiredException();
        }
        ClientAuthenticationRequest authenticationRequest = convertToClientAuthenticationRequest(exchange);
        try (ExchangeAuthenticationContext.Binding ignored = ExchangeAuthenticationContext.isolateCurrentThread()) {
            try {
                UserAuthentication authentication = authenticationClientTemplate
                        .authenticateUserByAuthenticationRequest(authenticationRequest);
                profileLoader.preparePersonProfile(authentication);

                // Resolve the trace allowlist while the validated Jwt is still available locally.
                observationTraceSupport.enrichGatewayAuthentication(exchange, authentication);

                ValidatedJwtBusinessContext jwtBusinessContext = validatedBusinessContext(authentication);
                UserAuthentication exchangeAuthentication = exchangeAuthentication(authentication);
                ExchangeAuthenticationContext.store(exchange, exchangeAuthentication, jwtBusinessContext);
                recordSecurityEvent(exchange, ScmSecurityEventType.AUTHENTICATION_SUCCESS, Map.of());
            } catch (Exception exception) {
                recordSecurityEvent(exchange, ScmSecurityEventType.AUTHENTICATION_FAILURE, Map.of(
                        "error.type", exception.getClass().getSimpleName(),
                        "error.code", exception.getClass().getSimpleName()
                ));
                throw exception;
            }
        }
    }

    private void removeJwtHeader(Exchange exchange) {
        if (exchange == null) {
            return;
        }
        exchange.getIn().removeHeader(JWT_HEADER);
        exchange.getMessage().removeHeader(JWT_HEADER);
    }

    private ValidatedJwtBusinessContext validatedBusinessContext(UserAuthentication authentication) {
        UserAuthentication.AuthenticationDetail details = authentication == null ? null : authentication.getDetails();
        Object loginData = details == null ? null : details.getLoginData();
        if (loginData instanceof Jwt jwt) {
            return new ValidatedJwtBusinessContext(normalizedRoles(jwt.getClaim("aut")));
        }
        return ValidatedJwtBusinessContext.empty();
    }

    private List<String> normalizedRoles(Object value) {
        Set<String> roles = new LinkedHashSet<>();
        appendRoles(roles, value);
        return List.copyOf(roles);
    }

    private void appendRoles(Set<String> roles, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof Collection<?> collection) {
            collection.forEach(item -> appendRoles(roles, item));
            return;
        }
        if (value.getClass().isArray()) {
            for (int index = 0; index < Array.getLength(value); index++) {
                appendRoles(roles, Array.get(value, index));
            }
            return;
        }
        String text = String.valueOf(value).trim();
        if (text.startsWith("[") && text.endsWith("]") && text.length() >= 2) {
            text = text.substring(1, text.length() - 1);
        }
        for (String item : text.split(",")) {
            if (!item.isBlank()) {
                roles.add(item.trim());
            }
        }
    }

    private UserAuthentication exchangeAuthentication(UserAuthentication authentication) {
        if (authentication == null) {
            return null;
        }
        UserAuthentication.AuthenticationDetail details = authentication.getDetails();
        UserAuthentication.AuthenticationDetail safeDetails = UserAuthentication.AuthenticationDetail.builder()
                .issuer(details == null ? null : details.getIssuer())
                .issuedAt(details == null ? null : details.getIssuedAt())
                .expiresAt(details == null ? null : details.getExpiresAt())
                .maxIdle(details == null ? null : details.getMaxIdle())
                .loginData(safeLoginData(details))
                .loginAccessParameter(details == null ? null : details.getLoginAccessParameter())
                .sessionId(details == null ? null : details.getSessionId())
                .clientId(details == null ? null : details.getClientId())
                .build();
        String delegatedUsername = authentication.isDelegated()
                ? authentication.getProfile().getNickname()
                : null;
        UserAuthentication copy = new UserAuthentication(
                safeDetails,
                authentication.getPrincipal(),
                delegatedUsername,
                authentication.getAuthorities()
        );
        copy.setAuthenticated(authentication.isAuthenticated());
        copy.setError(authentication.getError());
        if (authentication.getIsTransactionAuthenticated() != null) {
            copy.authenticateTransaction(authentication.getIsTransactionAuthenticated());
        }
        copyProfile(authentication.getProfile(), copy.getProfile());
        return copy;
    }

    private Object safeLoginData(UserAuthentication.AuthenticationDetail details) {
        return null;
    }

    private void copyProfile(UserProfile source, UserProfile target) {
        if (source == null || target == null) {
            return;
        }
        if (source.getPersonUsername() != null && source.getPersonId() != null) {
            target.loadPersonInfo(source.getPersonUsername(), source.getPersonId());
        }
        if (source.getMemberships() != null) {
            target.loadMembership(source.getMemberships());
        }
        target.setServiceAccesses(source.getServiceAccesses());
    }

    private void recordSecurityEvent(
            Exchange exchange,
            ScmSecurityEventType eventType,
            Map<String, ?> attributes
    ) {
        securityTraceEventRecorders.orderedStream().forEach(recorder -> {
            try {
                recorder.record(exchange, "gateway", eventType, attributes);
            } catch (RuntimeException ignored) {
                // Security trace enrichment must never alter authentication behavior.
            }
        });
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
