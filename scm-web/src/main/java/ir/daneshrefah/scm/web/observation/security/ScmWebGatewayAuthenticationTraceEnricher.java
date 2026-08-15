package ir.daneshrefah.scm.web.observation.security;

import ir.daneshrefah.scm.core.integration.observability.CoreObservationTraceSupport;
import ir.daneshrefah.scm.core.integration.observability.GatewayAuthenticationTraceEnricher;
import ir.daneshrefah.scm.core.integration.observability.gateway.GatewayObservationEnrichmentSupport;
import ir.daneshrefah.scm.core.integration.security.ExchangeAuthenticationContext;
import ir.daneshrefah.scm.observation.starter.ObservationScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
@Slf4j
public class ScmWebGatewayAuthenticationTraceEnricher implements GatewayAuthenticationTraceEnricher {
    private final ObjectProvider<GatewayAuthenticationTraceContributor> contributors;
    private final GatewayObservationEnrichmentSupport gatewayObservationEnrichmentSupport;

    public ScmWebGatewayAuthenticationTraceEnricher(
            ObjectProvider<GatewayAuthenticationTraceContributor> contributors,
            GatewayObservationEnrichmentSupport gatewayObservationEnrichmentSupport
    ) {
        this.contributors = contributors;
        this.gatewayObservationEnrichmentSupport = gatewayObservationEnrichmentSupport;
    }

    @Override
    public void enrich(Exchange exchange, Authentication authentication) {
        if (exchange == null) {
            return;
        }
        Set<String> requestedNames = gatewayObservationEnrichmentSupport.authenticatedAttributeNames(exchange);
        ExchangeAuthenticationContext.storeAuthenticatedAttributes(
                exchange,
                requestedNames,
                authenticatedAttributes(authentication, requestedNames)
        );
        Map<String, Object> attributes = attributes(authentication);
        if (attributes.isEmpty()) {
            return;
        }

        ObservationScope gatewayScope = exchange.getProperty(
                CoreObservationTraceSupport.GATEWAY_SCOPE_PROPERTY,
                ObservationScope.class
        );
        if (gatewayScope == null) {
            return;
        }
        gatewayScope.attributes(attributes);
    }

    private Map<String, Object> attributes(Authentication authentication) {
        Map<String, Object> attributes = new LinkedHashMap<>();
        contributors.orderedStream().forEach(contributor -> {
            try {
                if (contributor.supports(authentication)) {
                    Map<String, Object> contributed = contributor.attributes(authentication);
                    if (contributed != null) {
                        attributes.putAll(contributed);
                    }
                }
            } catch (RuntimeException exception) {
                log.warn("Authentication trace contributor failed contributorClass={} failureType={}",
                        contributor.getClass().getName(),
                        exception.getClass().getSimpleName());
            }
        });
        return attributes.isEmpty() ? Map.of() : Map.copyOf(attributes);
    }

    private Map<String, Object> authenticatedAttributes(Authentication authentication, Set<String> requestedNames) {
        if (requestedNames == null || requestedNames.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> attributes = new LinkedHashMap<>();
        contributors.orderedStream().forEach(contributor -> {
            try {
                if (contributor.supports(authentication)) {
                    Map<String, Object> contributed = contributor.authenticatedAttributes(authentication, requestedNames);
                    if (contributed != null) {
                        attributes.putAll(contributed);
                    }
                }
            } catch (RuntimeException exception) {
                log.warn("Authentication trace contributor failed contributorClass={} failureType={}",
                        contributor.getClass().getName(),
                        exception.getClass().getSimpleName());
            }
        });
        return attributes.isEmpty() ? Map.of() : Map.copyOf(attributes);
    }
}
