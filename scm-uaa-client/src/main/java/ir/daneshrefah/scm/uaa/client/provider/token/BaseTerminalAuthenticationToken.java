package ir.daneshrefah.scm.uaa.client.provider.token;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-21
 */
@Getter
public abstract class BaseTerminalAuthenticationToken extends BaseAuthenticationToken {

    private String username;
    private String terminalCode;
    private String clientId;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public BaseTerminalAuthenticationToken(String username, String terminalCode, String clientId,
                                           Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
//        Assert.hasText(terminalCode, "terminalCode cannot be empty");
        this.username = username;
        this.terminalCode = terminalCode;
        this.clientId = clientId;
    }

    @Override
    public Object getPrincipal() {
        return this.getUsername();
    }

}
