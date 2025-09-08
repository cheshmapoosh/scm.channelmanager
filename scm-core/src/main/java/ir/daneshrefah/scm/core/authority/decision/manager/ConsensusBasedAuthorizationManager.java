package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.SecurityContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

/***
 *  Accepted if 'ACCESS_GRANTED' are more than 'ACCESS_DENIED', if both are same then 'ACCESS_GRANTED' wins.
 */
@Component
@RequiredArgsConstructor
public class ConsensusBasedAuthorizationManager implements AuthorizationManager<SecurityContext.ManagerSecurityContext> {

    @Override
    public void verify(Supplier<Authentication> authentication, SecurityContext.ManagerSecurityContext securityContext) {
        List<? extends AuthorizationManager<SecurityContext>> authorities = securityContext.getAuthorities();
        int grants = 0;
        int denies = 0;
        for (AuthorizationManager<SecurityContext> authority : authorities) {
            AuthorizationResult check = authority.check(authentication, securityContext);
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

        throw new AccessDeniedException("access", ERROR_CODE_ACCESS_DENIED, "User does not have access to the process.");
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, SecurityContext.ManagerSecurityContext securityContext) {
        try {
            verify(authentication, securityContext);
            return new AuthorizationDecision(true);
        } catch (AuthorizationDeniedException e) {
            return new AuthorizationDecision(false);
        }
    }
}
