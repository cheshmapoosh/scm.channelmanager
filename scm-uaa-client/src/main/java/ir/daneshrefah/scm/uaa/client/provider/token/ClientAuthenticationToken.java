package ir.daneshrefah.scm.uaa.client.provider.token;

import ir.daneshrefah.scm.utils.string.HashUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;

import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
public class ClientAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final Object principal;

    private final Object credentials;

    public ClientAuthenticationToken(String terminalCode, Object principal, Object credentials) {
        super((String) principal, terminalCode, (String) principal, Collections.emptyList());
        this.principal = principal;
        this.credentials = credentials;
    }

    public static ClientAuthenticationToken unauthenticated(String terminalCode, Object principal, Object credentials) {
        return new ClientAuthenticationToken(terminalCode, principal, credentials);
    }

    @Override
    public Object getCredentials() {
        return this.credentials;
    }

    @Override
    public String getSessionCacheKey() {
        return this.getClass().getSimpleName() + "_" + HashUtils.hashMD5ToString(getName() +
                StringUtils.replaceNullWithSpace((String) credentials));
    }

}
