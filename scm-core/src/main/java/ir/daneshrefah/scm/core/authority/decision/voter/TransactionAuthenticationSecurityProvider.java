package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.core.authority.decision.configuration.model.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class TransactionAuthenticationSecurityProvider implements AuthorizationManager<SecurityContext> {

    //TODO


    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, SecurityContext securityContext) {
        return null;
    }

}
