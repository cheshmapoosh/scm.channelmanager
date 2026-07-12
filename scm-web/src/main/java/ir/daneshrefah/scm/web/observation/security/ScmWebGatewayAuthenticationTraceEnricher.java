package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.observability.GatewayAuthenticationTraceEnricher;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import ir.daneshrefah.scm.observation.starter.attributes.trace.CommonTraceAttributes;
import ir.daneshrefah.scm.web.observation.attributes.WebTraceAttributes;
import org.apache.camel.Exchange;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class ScmWebGatewayAuthenticationTraceEnricher implements GatewayAuthenticationTraceEnricher {
    public static final String JWT_CONTEXT_PROPERTY = "scm.observation.context.gateway.jwt";

    private final GatewayJwtTraceContextResolver contextResolver;

    public ScmWebGatewayAuthenticationTraceEnricher(GatewayJwtTraceContextResolver contextResolver) {
        this.contextResolver = contextResolver;
    }

    @Override
    public void enrich(Exchange exchange, Authentication authentication) {
        if (exchange == null) {
            return;
        }
        GatewayJwtTraceContext context = resolve(authentication);
        if (context.isEmpty()) {
            exchange.removeProperty(JWT_CONTEXT_PROPERTY);
            return;
        }
        exchange.setProperty(JWT_CONTEXT_PROPERTY, context);

        ObservationScope gatewayScope = exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_SCOPE_PROPERTY,
                ObservationScope.class
        );
        if (gatewayScope == null) {
            return;
        }
        gatewayScope.attribute(WebTraceAttributes.USER_NICKNAME, context.nickname())
                .attribute(WebTraceAttributes.JWT_SCOPE, nonEmpty(context.scopes()))
                .attribute(WebTraceAttributes.JWT_ISSUER, context.issuer())
                .attribute(WebTraceAttributes.CLIENT_ADDRESS, context.clientAddress())
                .attribute(WebTraceAttributes.JWT_ISSUE_AT, instant(context.issuedAt()))
                .attribute(WebTraceAttributes.JWT_EXPIRE_AT, instant(context.expiresAt()))
                .attribute(CommonTraceAttributes.SCM_CHANNEL_CODE, context.channelCode())
                .attribute(WebTraceAttributes.JWT_AUDIENCE, nonEmpty(context.audiences()))
                .attribute(WebTraceAttributes.JWT_GENERATOR, context.generator())
                .attribute(WebTraceAttributes.AUTH_TRANSACTION_METHOD, context.transactionMethod())
                .attribute(WebTraceAttributes.AUTH_LOGIN_METHOD, context.loginMethod());
    }

    private GatewayJwtTraceContext resolve(Authentication authentication) {
        GatewayJwtTraceContext context = contextResolver.resolve(authentication);
        return context == null ? GatewayJwtTraceContext.empty() : context;
    }

    private List<String> nonEmpty(List<String> values) {
        return values == null || values.isEmpty() ? null : values;
    }

    private String instant(Instant value) {
        return value == null ? null : value.toString();
    }
}
