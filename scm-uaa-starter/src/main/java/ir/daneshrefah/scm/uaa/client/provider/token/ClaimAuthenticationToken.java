package ir.daneshrefah.scm.uaa.client.provider.token;

import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-08
 */
public class ClaimAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final String username;

    private final String credentials;

    public ClaimAuthenticationToken(String terminalCode, String clientId, String accessParameter, String username, String credentials) {
        super(username, terminalCode, clientId, accessParameter, Collections.emptyList());
        this.username = username;
        this.credentials = credentials;
    }

    public static ClaimAuthenticationToken unauthenticated(String terminalCode, String clientId, String accessParameter,
                                                           String username, String credentials) {
        return new ClaimAuthenticationToken(terminalCode, clientId, accessParameter, username, credentials);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public String getSessionCacheKey() {
        return null; // return null that token not be cached.
    }

}
