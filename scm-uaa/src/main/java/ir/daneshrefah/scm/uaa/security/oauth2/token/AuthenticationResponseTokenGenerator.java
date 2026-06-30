package ir.daneshrefah.scm.uaa.security.oauth2.token;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Objects;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLAIM_KEY_LOGIN_AUTH_METHOD;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLAIM_KEY_TERMINAL;

@Component
@AllArgsConstructor
public class AuthenticationResponseTokenGenerator {
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
//    private final ObjectMapper objectMapper;

    public OAuth2AccessTokenAuthenticationToken getAccessToken(Authentication authentication, Authentication clientPrincipal,
                                                               RegisteredClient registeredClient,
                                                               UserLoginAuthenticationToken authorization) {
        Set<String> scopes = authorization.getDetails().getScopes();
        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authorization)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
//                .authorization(authorization)
                .authorizedScopes(scopes)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrant(authentication);


        OAuth2TokenContext tokenContext = tokenContextBuilder
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);

        OAuth2AccessToken accessToken = getAccessTokenValue(tokenContext, generatedAccessToken);

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken/*, refreshToken, additionalParameters*/);
    }

    private OAuth2AccessToken getAccessTokenValue(OAuth2TokenContext tokenContext, OAuth2Token generatedAccessToken) {
        Assert.isAssignable(AuthenticationOutcomeToken.class, tokenContext.getPrincipal().getClass());
        AuthenticationOutcomeToken.AuthenticationStatus authenticationStatus = ((AuthenticationOutcomeToken) tokenContext.getPrincipal()).getAuthenticationStatus();
        String tokenValue;
        if (Objects.requireNonNull(authenticationStatus) == AuthenticationOutcomeToken.AuthenticationStatus.AUTHENTICATED) {
            tokenValue = generatedAccessToken.getTokenValue();
        } else {
            tokenValue = generateClaimAccessToken(tokenContext.getPrincipal());//tokenContext.getPrincipal().getName();
        }
        Assert.isAssignable(AuthenticationOutcomeToken.class, tokenContext.getPrincipal().getClass());

        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                tokenValue, generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
    }

    private String generateClaimAccessToken(AuthenticationOutcomeToken principal) {
        ObjectNode token = JsonNodeFactory.instance.objectNode();
        token.put(JwtClaimNames.SUB, principal.getName());
        token.put(CLAIM_KEY_TERMINAL, principal.getTerminalCode());
        token.put(CLAIM_KEY_LOGIN_AUTH_METHOD, principal.getPrincipal().getUser().getLoginAuthenticationMethod().getCode());
        return token.toString();
    }


}
