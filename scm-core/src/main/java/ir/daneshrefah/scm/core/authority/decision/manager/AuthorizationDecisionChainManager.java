package ir.daneshrefah.scm.core.authority.decision.manager;

import org.apache.camel.Exchange;

public interface AuthorizationDecisionChainManager {
    void decide(Exchange exchange);
}
