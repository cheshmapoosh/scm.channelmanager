package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.core.authority.decision.constant.Vote;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionAuthenticationSecurityProvider extends SecurityProvider {

    //TODO

    @Override
    protected Vote apply(Exchange exchange) {
        return null;
    }

    @Override
    protected boolean support(Exchange exchange) {
        return false;
    }
}
