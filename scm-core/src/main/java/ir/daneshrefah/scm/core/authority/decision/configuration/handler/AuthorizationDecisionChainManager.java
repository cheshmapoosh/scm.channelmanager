package ir.daneshrefah.scm.core.authority.decision.configuration.handler;

import org.apache.camel.Exchange;

public interface AuthorizationDecisionChainManager {
    void decide(Exchange exchange);
}
