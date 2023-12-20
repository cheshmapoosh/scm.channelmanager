package ir.daneshrefah.scm.uaa.client.provider.token;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.util.Assert;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
import java.util.Collections;

public class SessionAuthenticationToken extends AbstractAuthenticationToken {

    private final String sessionKey;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param sessionKey
     *
     */
    public SessionAuthenticationToken(String sessionKey) {
        super(Collections.emptyList());
        Assert.hasText(sessionKey, "sessionKey cannot be empty");
        this.sessionKey = sessionKey;
    }

    @Override
    public Object getCredentials() {
        return this.sessionKey;
    }

    @Override
    public Object getPrincipal() {
        return this.sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }
}
