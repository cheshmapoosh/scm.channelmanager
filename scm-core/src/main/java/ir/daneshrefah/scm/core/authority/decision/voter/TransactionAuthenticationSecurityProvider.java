package ir.daneshrefah.scm.core.authority.decision.voter;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionAuthenticationSecurityProvider implements AuthorizationManager<Exchange> {

    //TODO


    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, Exchange object) {
        return null;
    }

}
