package ir.daneshrefah.scm.uaa.security.oauth2.grant.smsotp;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Getter
public class SmsOtpGrantAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private final String credentials;

    public SmsOtpGrantAuthenticationToken(
            Object principal,
            String credentials,
            Set<String> scopes,
            Authentication clientPrincipal
    ) {
        this(principal, credentials, scopes, clientPrincipal, null);
    }

    public SmsOtpGrantAuthenticationToken(
            Object principal,
            String credentials,
            Set<String> scopes,
            Authentication clientPrincipal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(scopes, clientPrincipal, authorities);
        this.principal = principal;
        this.credentials = credentials;
        if (Objects.nonNull(authorities) && !authorities.isEmpty()) {
            setAuthenticated(true);
        }
    }

    @Override
    public boolean includeChallengeCode() {
        return true;
    }

    @Override
    public AuthorizationGrantType getGrantType() {
        return AuthorizationGrantType.SMS_OTP;
    }
}
