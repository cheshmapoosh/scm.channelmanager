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

public class SessionAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final String sessionKey;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param sessionKey
     *
     */
    public SessionAuthenticationToken(String terminalCode, String sessionKey) {
        super(terminalCode, Collections.emptyList());
        Assert.hasText(sessionKey, "sessionKey cannot be empty");
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    @Override
    public Object getCredentials() {
        return this.getSessionKey();
    }

    @Override
    public Object getPrincipal() {
        return this.getSessionKey();
    }

}
