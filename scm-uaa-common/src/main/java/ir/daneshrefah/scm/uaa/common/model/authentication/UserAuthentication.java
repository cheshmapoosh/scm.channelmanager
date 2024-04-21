package ir.daneshrefah.scm.uaa.common.model.authentication;

import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
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

import static ir.daneshrefah.scm.common.constant.RoleConstants.ROLE_ANONYMOUS;

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
    private final UserProfile profile;
    @Setter
    private String error;

    /**
     * @param details
     * @param principal
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
        this(details, principal, null, authorities);
    }

    /**
     * Creates a delegated token with the supplied array of authorities.
     *
     * @param principal
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public UserAuthentication(AuthenticationDetail details, User principal, String delegatedUsername, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        setDetails(details);
        this.principal = principal;
        setAuthenticated(null != authorities);
        if (StringUtils.isNotEmpty(delegatedUsername)) {
            profile = new UserProfile(delegatedUsername);
        } else if (null != principal && isAuthenticated()) {
            profile = new UserProfile(principal.getNickname(), principal.getPerson().getUsername(), principal.getPerson().getId().longValue());
        } else {
            profile = new UserProfile();
        }
    }


    @Override
    public String getTerminalCode() {
        return null != principal ? principal.getTerminalCode() : null;
    }

    @Override
    public boolean isAnonymous() {
        return hasAuthority(ROLE_ANONYMOUS);
    }

    @Override
    public boolean isDelegated() {
        String delegatedUsername = profile.getNickname();
        return isFullyAuthenticated() && StringUtils.isNotEmpty(delegatedUsername) &&
                StringUtils.notEquals(getName(), delegatedUsername);
    }

    @Override
    public boolean isFullyAuthenticated() {
        return isAuthenticated() && !isAnonymous();
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
    public AuthenticationMethod getAuthenticationMethod() {
        return null != principal ? principal.getLoginAuthenticationMethod() : null;
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

    @Builder
    @Getter
    public static class AuthenticationDetail implements Serializable {
        private final String issuer;
        private final Instant issuedAt;
        private final Instant expiresAt;
        private final Duration maxIdle;
        private final Object loginData;
        private final String loginAccessParameter;
        private final String sessionId;
        private final String clientId;
    }

}
