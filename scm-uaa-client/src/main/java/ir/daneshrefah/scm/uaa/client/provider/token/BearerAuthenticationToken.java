package ir.daneshrefah.scm.uaa.client.provider.token;

import ir.daneshrefah.scm.utils.string.HashUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.util.Assert;

import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BearerAuthenticationToken extends BaseTerminalAuthenticationToken {

    private final String token;

    public BearerAuthenticationToken(String username, String terminalCode, String token) {
        super(username, terminalCode, Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @Override
    public Object getCredentials() {
        return this.getToken();
    }

    @Override
    public String getSessionCacheKey() {
        return HashUtils.hashMD5ToString(getToken()) + StringUtils.DOUBLE_COLON + getTerminalCode();
    }
}
