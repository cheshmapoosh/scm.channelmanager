package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.core.authority.decision.configuration.model.SecurityContext;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class RoleCheckAuthorizationManager implements AuthorizationManager<SecurityContext> {


    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, SecurityContext securityContext) {
        return Optional
                .ofNullable(securityContext.getServiceAcceptableRoles())
                .filter(accessRoles -> !accessRoles.isEmpty())
                .map(accessRoles -> {
                    List<String> userRoles = securityContext.getUserRoles();
                    return accessRoles
                            .stream()
                            .filter(userRoles::contains)
                            .map(role -> new AuthorizationDecision(true))
                            .findFirst()
                            .orElseGet(() -> new AuthorizationDecision(false));
                }).orElse(null);
    }

}
