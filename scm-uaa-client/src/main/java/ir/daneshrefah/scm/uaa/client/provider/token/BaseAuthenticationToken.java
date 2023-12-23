package ir.daneshrefah.scm.uaa.client.provider.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-21
 */
public abstract class BaseAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public BaseAuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }

    public abstract String getId();

}
