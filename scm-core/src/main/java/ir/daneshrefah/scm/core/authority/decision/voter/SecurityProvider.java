package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.core.authority.decision.constant.Vote;
import org.apache.camel.Exchange;

public abstract class SecurityProvider {

    public final Vote getVote(Exchange exchange) {
        boolean isSupport = support(exchange);
        if (!isSupport) {
            return Vote.ACCESS_ABSTAIN;
        }
        return apply(exchange);
    }

    protected abstract Vote apply(Exchange exchange);

    protected abstract boolean support(Exchange exchange);


}
