package ir.daneshrefah.scm.uaa.client.provider.token;

import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BasicAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final Object principal;

    private final Object credentials;

    public BasicAuthenticationToken(String terminalCode, Object principal, Object credentials) {
        super(terminalCode, Collections.emptyList());
        this.principal = principal;
        this.credentials = credentials;
    }

    public static BasicAuthenticationToken unauthenticated(String terminalCode, Object principal, Object credentials) {
        return new BasicAuthenticationToken(terminalCode, principal, credentials);
    }

    @Override
    public Object getCredentials() {
        return this.principal;
    }

    @Override
    public Object getPrincipal() {
        return this.credentials;
    }
}
