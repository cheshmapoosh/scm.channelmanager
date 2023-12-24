package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
public class PostAuthenticationToken extends GeneralAuthenticationToken {

    private final AuthenticationStatus authenticationStatus;

    protected PostAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken,
                                      AuthenticationStatus authenticationStatus) {
        super(user, preAuthenticationToken);
        this.authenticationStatus = authenticationStatus;
    }

    protected PostAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken,
                                      Collection<? extends GrantedAuthority> authorities) {
        super(user, preAuthenticationToken, authorities);
        this.authenticationStatus = AuthenticationStatus.AUTHENTICATED;
        setAuthenticated(true);
    }

    public static PostAuthenticationToken unauthenticated(TerminalUserDetails user,
                                                          PreAuthenticationToken preAuthenticationToken) {
        return new PostAuthenticationToken(user, preAuthenticationToken, AuthenticationStatus.UN_AUTHENTICATED);
    }

    public static PostAuthenticationToken authenticated(TerminalUserDetails user,
                                                        PreAuthenticationToken preAuthenticationToken,
                                                        Collection<? extends GrantedAuthority> authorities) {
        return new PostAuthenticationToken(user, preAuthenticationToken, authorities);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    public enum AuthenticationStatus {
        UN_AUTHENTICATED,
        CONTINUE,
        AUTHENTICATED
    }
}
