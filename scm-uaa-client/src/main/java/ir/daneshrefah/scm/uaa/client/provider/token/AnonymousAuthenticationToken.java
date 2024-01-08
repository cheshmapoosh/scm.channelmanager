package ir.daneshrefah.scm.uaa.client.provider.token;

import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class AnonymousAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final int keyHash;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param username
     * @param terminalCode
     * @param authorities  the collection of <tt>GrantedAuthority</tt>s for the principal
     *                     represented by this authentication object.
     */
    public AnonymousAuthenticationToken(String username, String terminalCode, Collection<? extends GrantedAuthority> authorities) {
        super(username, terminalCode, authorities);
        this.keyHash = extractKeyHash(username);
    }

    private static Integer extractKeyHash(String key) {
        Assert.hasLength(key, "key cannot be empty or null");
        return key.hashCode();
    }

    public int getKeyHash() {
        return keyHash;
    }

    @Override
    public String getSessionCacheKey() {
        return getClass().getSimpleName() + getTerminalCode();
    }

    @Override
    public Object getCredentials() {
        return StringUtils.EMPTY;
    }
}
