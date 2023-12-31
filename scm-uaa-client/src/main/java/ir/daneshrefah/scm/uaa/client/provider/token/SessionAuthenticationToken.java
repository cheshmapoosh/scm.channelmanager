package ir.daneshrefah.scm.uaa.client.provider.token;

import ir.daneshrefah.scm.utils.string.StringUtils;
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

    private final String sessionId;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param sessionId
     *
     */
    public SessionAuthenticationToken(String username, String terminalCode, String sessionId) {
        super(username, terminalCode, Collections.emptyList());
        Assert.hasText(sessionId, "sessionKey cannot be empty");
        this.sessionId = sessionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public Object getCredentials() {
        return this.getSessionId();
    }

    @Override
    public String getId() {
        return getSessionId() + StringUtils.DOUBLE_COLON + getTerminalCode();
    }
}
