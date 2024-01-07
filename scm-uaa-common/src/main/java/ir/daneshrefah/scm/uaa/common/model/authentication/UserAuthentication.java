package ir.daneshrefah.scm.uaa.common.model.authentication;

import ir.daneshrefah.scm.common.model.message.IAuthenticationHeader;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-08-14
 */
@Getter
public class UserAuthentication implements Authentication, IAuthenticationHeader {

    private final Collection<GrantedAuthority> authorities;
    private AuthenticationDetail details;
    private boolean authenticated = false;
    private User principal;
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
        this.details = details;
        this.principal = principal;
        setAuthenticated(null != authorities);
        if (authorities == null) {
            this.authorities = AuthorityUtils.NO_AUTHORITIES;
            return;
        }
        for (GrantedAuthority a : authorities) {
            Assert.notNull(a, "Authorities collection cannot contain any null elements");
        }
        this.authorities = Collections.unmodifiableList(new ArrayList<>(authorities));
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
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return null != principal ? principal.getNickName() : null;
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
