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
import java.util.function.Supplier;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_ACCESS_DENIED;

/**
 * Grants access with the first 'ACCESS_GRANTED' and deny with any 'ACCESS_DENIED' vote.
 */
@Component
@RequiredArgsConstructor
public class AffirmativeBasedAuthorizationManager implements AuthorizationManager<SecurityContext.ManagerSecurityContext> {

    @Override
    public void verify(Supplier<Authentication> authentication, SecurityContext.ManagerSecurityContext securityContext) {
        List<? extends AuthorizationManager<SecurityContext>> authorities = securityContext.getAuthorities();
        boolean granted = false;
        for (AuthorizationManager<SecurityContext> authority : authorities) {
            AuthorizationResult result = authority.check(authentication, securityContext);
            if (result != null) {
                if (!result.isGranted()) {
                    throw new AccessDeniedException("access", ERROR_CODE_ACCESS_DENIED, "User does not have access to the process.");
                } else {
                    granted = true;
                    break;
                }
            }
        }

        if (!granted) {
            throw new AccessDeniedException("access", ERROR_CODE_ACCESS_DENIED, "User does not have access to the process.");
        }
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
