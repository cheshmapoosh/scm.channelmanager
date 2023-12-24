package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class GeneralAuthenticationToken extends AbstractAuthenticationToken {

    private final PreAuthenticationToken preAuthenticationToken;


    protected GeneralAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        this(user, preAuthenticationToken, Collections.emptyList());
    }

    protected GeneralAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.setDetails(user);
        this.preAuthenticationToken = preAuthenticationToken;
    }

    public PreAuthenticationToken getPreAuthenticationToken() {
        return preAuthenticationToken;
    }

    @Override
    public Object getCredentials() {
        return getPreAuthenticationToken().getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return getPreAuthenticationToken().getPrincipal();
    }

}
