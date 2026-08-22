package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-27
 */
@Getter
public class OAuth2SmsOtpAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String credentials;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param principal contains phoneNumber of user.
     * @param credentials contains claim code.
     * @param scopes
     * @param clientPrincipal
     *
     */
    public OAuth2SmsOtpAuthenticationToken(Object principal, String credentials,
                                           Set<String> scopes, Authentication clientPrincipal) {
        this(principal, credentials, scopes, clientPrincipal, null);
    }

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param principal contains phoneNumber of user.
     * @param credentials contains claim code.
     * @param scopes
     * @param clientPrincipal
     *
     */
    public OAuth2SmsOtpAuthenticationToken(Object principal, String credentials, Set<String> scopes,
                                           Authentication clientPrincipal, Collection<? extends GrantedAuthority> authorities) {
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
