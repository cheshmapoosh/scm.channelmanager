package ir.daneshrefah.scm.core.authority.decision.manager.impl;

import ir.daneshrefah.scm.core.authority.decision.manager.AuthorizationData;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/***
 *  Accepted if 'ACCESS_GRANTED' are more than 'ACCESS_DENIED', if both are same then 'ACCESS_GRANTED' wins.
 */
@Component
@RequiredArgsConstructor
public class ConsensusBasedAuthorizationManager implements AuthorizationManager<AuthorizationData> {

    @Override
    public void verify(Supplier<Authentication> authentication, AuthorizationData authorizationData) {
        Exchange exchange = authorizationData.getExchange();
        List<? extends AuthorizationManager<Exchange>> authorities = authorizationData.getAuthorities();
        int grants = 0;
        int denies = 0;
        for (AuthorizationManager<Exchange> authority : authorities) {
            AuthorizationResult check = authority.authorize(authentication, exchange);
            if (Objects.isNull(check)) {
                continue;
            }
            if (check.isGranted()) {
                grants++;
            } else {
                denies++;
            }
        }
        if (grants >= denies) {
            return;
        }

        throw new AuthorizationDeniedException("Access Denied");
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, AuthorizationData authorizationData) {
        try {
            verify(authentication, authorizationData);
            return new AuthorizationDecision(true);
        } catch (AuthorizationDeniedException e) {
            return new AuthorizationDecision(false);
        }
    }
}
