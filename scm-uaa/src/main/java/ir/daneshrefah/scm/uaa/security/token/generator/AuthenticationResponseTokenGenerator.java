package ir.daneshrefah.scm.uaa.security.token.generator;

import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Set;

@Component
@AllArgsConstructor
public class AuthenticationResponseTokenGenerator {
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public OAuth2AccessTokenAuthenticationToken getAccessToken(Authentication authentication, OAuth2ClientAuthenticationToken clientPrincipal, RegisteredClient registeredClient, GeneralAuthenticationToken authorization) {
        Set<String> scopes = authorization.getPreAuthenticationToken().getScopes();
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
        Assert.isAssignable(PostAuthenticationToken.class,tokenContext.getPrincipal().getClass());
        PostAuthenticationToken.AuthenticationStatus authenticationStatus = ((PostAuthenticationToken) tokenContext.getPrincipal()).getAuthenticationStatus();
        String tokenValue;
        switch (authenticationStatus){
            case AUTHENTICATED ->tokenValue=generatedAccessToken.getTokenValue();
            default -> tokenValue=tokenContext.getPrincipal().getName();
        }
        Assert.isAssignable(PostAuthenticationToken.class,tokenContext.getPrincipal().getClass());
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                tokenValue, generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
        return accessToken;
    }




}
