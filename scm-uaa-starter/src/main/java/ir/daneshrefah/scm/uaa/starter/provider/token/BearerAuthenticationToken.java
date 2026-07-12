package ir.daneshrefah.scm.uaa.starter.provider.token;

import ir.daneshrefah.scm.utils.string.HashUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;
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
    private transient Jwt validatedJwt;

    public BearerAuthenticationToken(String username, String terminalCode, String clientId, String accessParameter, String token) {
        super(username, terminalCode, clientId, accessParameter, Collections.emptyList());
        Assert.hasText(token, "token cannot be empty");
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public Jwt getValidatedJwt() {
        return validatedJwt;
    }

    public void validatedJwt(Jwt validatedJwt) {
        this.validatedJwt = validatedJwt;
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
