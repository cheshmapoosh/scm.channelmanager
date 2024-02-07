package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
public class PostAuthenticationToken extends GeneralAuthenticationToken {

    @Getter
    private final AuthenticationStatus authenticationStatus;
    @Getter
    @Setter
    private String sessionId;
    @Getter
    @Setter
    private Instant issuedAt;
    @Getter
    @Setter
    private Instant expiresAt;

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

    public static PostAuthenticationToken incomplete(TerminalUserDetails user,
                                                     PreAuthenticationToken preAuthenticationToken) {
        PostAuthenticationToken postAuthenticationToken = new PostAuthenticationToken(user, preAuthenticationToken, AuthenticationStatus.INCOMPLETE);
        postAuthenticationToken.setAuthenticated(true);
        return postAuthenticationToken;

    }

    public static PostAuthenticationToken secondLvlAuthenticated(TerminalUserDetails user,
                                                                 PreAuthenticationToken preAuthenticationToken) {
        PostAuthenticationToken postAuthenticationToken = new PostAuthenticationToken(user, preAuthenticationToken, AuthenticationStatus.SECOND_LEVEL_AUTHENTICATED);
        postAuthenticationToken.setAuthenticated(true);
        return postAuthenticationToken;

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

    public enum AuthenticationStatus {
        UN_AUTHENTICATED,
        INCOMPLETE,
        AUTHENTICATED,
        SECOND_LEVEL_AUTHENTICATED
    }

    @Override
    public String getName() {
        return String.valueOf(this.getDetails().getPrincipal());
    }

    public String getTerminalCode() {
        if (null == getPrincipal() || !(getPrincipal() instanceof TerminalUserDetails)
                || null == ((TerminalUserDetails) getPrincipal()).getUser()) {
            return null;
        }
        return ((TerminalUserDetails) getPrincipal()).getUser().getTerminalCode();
    }

}
