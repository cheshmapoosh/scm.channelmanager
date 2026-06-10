package ir.daneshrefah.scm.uaa.client.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.StringUtils;

public class ScmOwnershipGuard {
    private final ScmSecurityContext securityContext;

    public ScmOwnershipGuard(ScmSecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public void requireOwnership(ScmOwnedResource resource) {
        if (resource == null) {
            throw new AccessDeniedException("Owned resource is required");
        }
        ScmPrincipal principal = securityContext.requirePrincipal();
        boolean checked = false;

        if (StringUtils.hasText(resource.ownerSubject())) {
            checked = true;
            requireMatch(resource.ownerSubject(), principal.subject(), "Resource owner subject does not match authenticated principal");
        }
        if (StringUtils.hasText(resource.ownerSessionId())) {
            checked = true;
            requireMatch(resource.ownerSessionId(), principal.sessionId(), "Resource owner session does not match authenticated principal");
        }
        if (StringUtils.hasText(resource.ownerTerminalCode())) {
            checked = true;
            requireMatch(resource.ownerTerminalCode(), principal.terminalCode(), "Resource owner terminal does not match authenticated principal");
        }
        if (!checked) {
            throw new AccessDeniedException("Resource ownership metadata is missing");
        }
    }

    private void requireMatch(
            String expected,
            String actual,
            String message
    ) {
        if (!StringUtils.hasText(expected) || !StringUtils.hasText(actual) || !expected.trim().equals(actual.trim())) {
            throw new AccessDeniedException(message);
        }
    }
}
