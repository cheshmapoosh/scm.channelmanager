package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import org.apache.camel.Exchange;

public interface SecurityDecisionManager {
    void decide(Exchange exchange) throws AuthorityBaseException;
}
