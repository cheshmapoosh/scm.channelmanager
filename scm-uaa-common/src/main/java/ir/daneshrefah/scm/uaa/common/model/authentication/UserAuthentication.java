package ir.daneshrefah.scm.uaa.common.model.authentication;

import ir.daneshrefah.scm.common.model.message.IAuthenticationHeader;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
@Getter
@Setter
public class UserAuthentication extends AbstractAuthenticationToken implements IAuthenticationHeader {

    private String issuer;
//    private String username;
    private Instant issuedAt;
    private Instant expiresAt;
    private Duration maxIdle;
    private Object loginData;
    private String loginAccessParameter;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public UserAuthentication(TerminalUserDetails principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.setDetails(principal);
    }


    @Override
    public String getUsername() {
        return getUserDetails().getUsername();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return getDetails();
    }

    public TerminalUserDetails getUserDetails() {
        return (TerminalUserDetails) getDetails();
    }

    public boolean isAnonymous() {
        return hasAuthority("ROLE_ANONYMOUS");
    }

    public boolean hasAuthority(String authorityName) {
        Collection<GrantedAuthority> authorities = getAuthorities();
        if (null == authorities || authorities.isEmpty()) {
            return false;
        }
        return getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(authorityName));
    }
}
