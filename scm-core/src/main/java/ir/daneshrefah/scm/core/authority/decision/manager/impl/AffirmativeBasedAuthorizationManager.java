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

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Grants access with the first 'ACCESS_GRANTED' and deny with any 'ACCESS_DENIED' vote.
 */
@Component
@RequiredArgsConstructor
public class AffirmativeBasedAuthorizationManager implements AuthorizationManager<AuthorizationData> {


    @Override
    public void verify(Supplier<Authentication> authentication, AuthorizationData authorizationData) {
        Exchange exchange = authorizationData.getExchange();
        boolean granted = authorizationData.getAuthorities().stream()
                .map(a -> a.authorize(authentication, exchange))
                .filter(Objects::nonNull)
                .peek(r -> {
                    if (!r.isGranted()) {
                        throw new AuthorizationDeniedException("Access Denied");
                    }
                })
                .anyMatch(AuthorizationResult::isGranted);
        if (!granted) {
            throw new AuthorizationDeniedException("Access Denied");
        }
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
