package ir.daneshrefah.scm.uaa.common.model.authentication;

import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
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
public class UserAuthentication extends AbstractAuthenticationToken implements Authentication {

    private User principal;
    private PersonProfile profile;
    @Setter
    private String error;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param principal
     *
     */
    public UserAuthentication(AuthenticationDetail details, User principal) {
        this(details, principal, null);
    }

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param principal
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public UserAuthentication(AuthenticationDetail details, User principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        setDetails(details);
        this.principal = principal;
        setAuthenticated(null != authorities);
        if (null != principal && isAuthenticated()) {
            profile = new PersonProfile(principal.getPerson().getUsername(), principal.getPerson().getId().longValue());
        }
    }


    @Override
    public String getTerminalCode() {
        return null != principal ? principal.getTerminalCode() : null;
    }

    @Override
    public boolean isAnonymous() {
        return hasAuthority("ROLE_ANONYMOUS");
    }

    @Override
    public boolean hasAuthority(String authorityName) {
        Collection<GrantedAuthority> authorities = getAuthorities();
        if (null == authorities || authorities.isEmpty()) {
            return false;
        }
        return getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals(authorityName));
    }

    @Override
    public boolean hasError() {
        return StringUtils.isNotEmpty(error);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public String getName() {
        return null != principal ? principal.getNickname() : null;
    }

    @Override
    public AuthenticationDetail getDetails() {
        return (AuthenticationDetail) super.getDetails();
    }

    @Override
    public String getPersonUsername() {
        return null != principal && null != principal.getPerson() ? principal.getPerson().getUsername() : null;
    }

    @Override
    public PersonProfile getPersonProfile() {
        return profile;
    }


    @Builder
    @Getter
    public static class AuthenticationDetail implements Serializable {
        private String issuer;
        private Instant issuedAt;
        private Instant expiresAt;
        private Duration maxIdle;
        private Object loginData;
        private String loginAccessParameter;
        private String sessionId;
        private String clientId;
    }

}
