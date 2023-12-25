package ir.daneshrefah.scm.uaa.client.converter.token;

import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-25
 */
@Component
public class JwtTokenConverter implements TokenConverter<String> {

    private final JwtDecoder jwtDecoder;

    public JwtTokenConverter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public UserAuthentication convert(String token) {
        Jwt jwt = getJwt(token);
        URL issuer = jwt.getIssuer(); //JwtClaimNames.ISS
        String clientId = jwt.getAudience().get(0);
        String username = StringUtils.isNotEmpty(jwt.getSubject()) ? jwt.getSubject() : clientId;
        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();
        String terminalCode = jwt.getClaimAsString(Constants.JWT_CLAIM_NAME_TERMINAL);
        Collection<GrantedAuthority> authorities = Collections.emptyList();

        User user = new User();
        user.setTerminalCode(terminalCode);
        user.setNickName(username);
        user.setActive(true); //TODO
        TerminalUserDetails userDetails = new TerminalUserDetails(user);
        UserAuthentication result = new UserAuthentication(userDetails, authorities);
        result.setIssuer(issuer.toString());
        result.setUsername(username);
        result.setIssuedAt(issuedAt);
        result.setExpiresAt(expiresAt);
        result.setAuthenticated(true);
        result.setLoginData(jwt);

        return result;
    }

    private Jwt getJwt(String token) {
        try {
            return this.jwtDecoder.decode(token);
        }
        catch (BadJwtException failed) {
//            this.logger.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        }
        catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }
}
