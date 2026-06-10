package ir.daneshrefah.scm.uaa.client.provider.token;

import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BasicAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final String username;

    private final String credentials;

    public BasicAuthenticationToken(String terminalCode, String clientId, String accessParameter, String username, String credentials) {
        super(username, terminalCode, clientId, accessParameter, Collections.emptyList());
        this.username = username;
        this.credentials = credentials;
    }

    public static BasicAuthenticationToken unauthenticated(String terminalCode, String clientId, String accessParameter, String username, String credentials) {
        return new BasicAuthenticationToken(terminalCode, clientId, accessParameter, username, credentials);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public String getSessionCacheKey() {
        return getClass().getSimpleName() + "_" + getName() + StringUtils.DOUBLE_COLON + getTerminalCode();
    }

}
