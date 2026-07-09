package ir.daneshrefah.scm.uaa.starter.security;

import java.util.Set;

public record ScmPrincipal(
        String subject,
        String sessionId,
        String nickname,
        String terminalCode,
        String clientId,
        String tokenId,
        Set<String> roles,
        Set<String> scopes
) {
    public ScmPrincipal {
        roles = roles == null ? Set.of() : Set.copyOf(roles);
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }
}
