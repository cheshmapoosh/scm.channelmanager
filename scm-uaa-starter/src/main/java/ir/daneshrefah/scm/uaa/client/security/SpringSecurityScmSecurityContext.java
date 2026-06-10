package ir.daneshrefah.scm.uaa.client.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

public class SpringSecurityScmSecurityContext implements ScmSecurityContext {

    @Override
    public ScmPrincipal requirePrincipal() {
        return currentPrincipal().orElseThrow(() ->
                new AuthenticationCredentialsNotFoundException("Authenticated SCM principal is required"));
    }

    @Override
    public Optional<ScmPrincipal> currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof ScmPrincipal scmPrincipal) {
            return Optional.of(scmPrincipal);
        }
        if (principal instanceof Jwt jwt) {
            return Optional.of(new ScmPrincipal(
                    jwt.getSubject(),
                    jwt.getClaimAsString("sid"),
                    jwt.getClaimAsString("nickname"),
                    jwt.getClaimAsString("terminalCode"),
                    jwt.getClaimAsString("client_id"),
                    jwt.getId(),
                    java.util.Set.of(),
                    java.util.Set.of()
            ));
        }
        return Optional.empty();
    }
}
