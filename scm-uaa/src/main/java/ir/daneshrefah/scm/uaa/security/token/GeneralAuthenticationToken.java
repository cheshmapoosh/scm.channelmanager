package ir.daneshrefah.scm.uaa.security.token;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class GeneralAuthenticationToken extends AbstractAuthenticationToken {

    @Getter
    @Setter
    private boolean sessionRequired;
    @Getter
    @Setter
    private boolean notificationRequired;
    private final TerminalUserDetails principal;
//    private TerminalUserDetails userDetails;


    public GeneralAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken) {
        this(user, preAuthenticationToken, Collections.emptyList());
    }

    public GeneralAuthenticationToken(TerminalUserDetails user, PreAuthenticationToken preAuthenticationToken,
                                         Collection<? extends GrantedAuthority> authorities) {
        super(Objects.nonNull(authorities) && !authorities.isEmpty() ? authorities : user.getAuthorities());
        this.setDetails(preAuthenticationToken);
        this.principal = user;
    }

    @Override
    public Object getCredentials() {
        return getDetails().getCredentials();
    }

    @Override
    public TerminalUserDetails getPrincipal() {
        return this.principal;
    }

    @Override
    public PreAuthenticationToken getDetails() {
        return (PreAuthenticationToken) super.getDetails();
    }
}
