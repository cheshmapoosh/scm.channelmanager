package ir.daneshrefah.scm.uaa.security.form;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import org.springframework.security.core.Authentication;

import java.util.Set;

public record UaaFormLoginAuthenticationRequest(
        String username,
        String password,
        String clientId,
        String claimCode,
        Set<String> scopes,
        Authentication sourceAuthentication
) {
    public AuthorizationGrantType grantType() {
        return AuthorizationGrantType.FIRST_PASSWORD;
    }
}
