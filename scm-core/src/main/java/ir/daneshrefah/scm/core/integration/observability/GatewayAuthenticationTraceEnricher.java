package ir.daneshrefah.scm.core.integration.observability;

import org.apache.camel.Exchange;
import org.springframework.security.core.Authentication;

@FunctionalInterface
public interface GatewayAuthenticationTraceEnricher {
    void enrich(Exchange exchange, Authentication authentication);
}
