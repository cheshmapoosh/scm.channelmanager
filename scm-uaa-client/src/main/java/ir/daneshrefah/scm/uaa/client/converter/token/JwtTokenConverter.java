package ir.daneshrefah.scm.uaa.client.converter.token;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

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
        String clientId = jwt.getAudience().get(0);
        String username = StringUtils.isNotEmpty(jwt.getSubject()) ? jwt.getSubject() : clientId;

        Collection<GrantedAuthority> authorities = Collections.emptyList();
        String commaSeparatedAuthorities = jwt.getClaimAsString(Constants.CLAIM_KEY_AUTHORITIES);
        if (StringUtils.isNotEmpty(commaSeparatedAuthorities)) {
            String[] authoritiesArray = commaSeparatedAuthorities.split(",");

            authorities = Arrays.stream(authoritiesArray)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        }
        AuthorizationGrantType grantType = AuthorizationGrantType.valueOf(jwt.getClaimAsString(Constants.CLAIM_KEY_GRANT));
        URL issuer = jwt.getIssuer(); //JwtClaimNames.ISS
        String terminalCode = jwt.getClaimAsString(Constants.CLAIM_KEY_TERMINAL);
        String sessionId = jwt.getClaimAsString(Constants.CLAIM_KEY_SESSION);
        AuthenticationMethod loginAuthenticationMethod = null;
        if (StringUtils.isNotEmpty(jwt.getClaimAsString(Constants.CLAIM_KEY_LOGIN_AUTH_METHOD))) {
            loginAuthenticationMethod = AuthenticationMethod.valueOf(
                    jwt.getClaimAsString(Constants.CLAIM_KEY_LOGIN_AUTH_METHOD));
        }
        AuthenticationMethod transactionAuthenticationMethod = null;
        if (StringUtils.isNotEmpty(jwt.getClaimAsString(Constants.CLAIM_KEY_TRANSACTION_AUTH_METHOD))) {
            transactionAuthenticationMethod = AuthenticationMethod.valueOf(
                    jwt.getClaimAsString(Constants.CLAIM_KEY_TRANSACTION_AUTH_METHOD));
        }
//        "scope" -> {ArrayList@23632}  size = 2
        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();


        User user = new User();
        user.setTerminalCode(terminalCode);
        user.setNickname(username);
        user.setLoginAuthenticationMethod(loginAuthenticationMethod);
        user.setTransactionAuthenticationMethod(transactionAuthenticationMethod);
        user.setActive(true); //TODO

        UserAuthentication.AuthenticationDetail detail = UserAuthentication.AuthenticationDetail.builder()
                .issuer(issuer.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .maxIdle(null)
                .loginData(jwt)
                .loginAccessParameter(null)
                .sessionId(sessionId)
                .clientId(clientId)
                .build();

        UserAuthentication result = new UserAuthentication(detail,
                user, authorities);

        return result;
    }

    private Jwt getJwt(String token) {
        try {
            return this.jwtDecoder.decode(token);
        } catch (BadJwtException failed) {
//            this.logger.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        } catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }
}
