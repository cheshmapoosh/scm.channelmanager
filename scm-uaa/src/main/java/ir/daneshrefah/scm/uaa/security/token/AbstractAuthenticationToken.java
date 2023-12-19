package ir.daneshrefah.scm.uaa.security.token;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public abstract class AbstractAuthenticationToken extends org.springframework.security.authentication.AbstractAuthenticationToken {
    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public AbstractAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }
}
