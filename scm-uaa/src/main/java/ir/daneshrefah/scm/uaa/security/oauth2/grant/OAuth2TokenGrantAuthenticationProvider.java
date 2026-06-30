package ir.daneshrefah.scm.uaa.security.oauth2.grant;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.mapper.AuthorizationGrantTypeMapper;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public abstract class OAuth2TokenGrantAuthenticationProvider<T extends AbstractAuthenticationToken> implements AuthenticationProvider {
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    @Override
    @SuppressWarnings("unchecked")
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        T authenticationToken = (T) authentication;
        AuthorizationGrantType grantType = grantType();

        String clientId = authenticationToken.getClientId();
        RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            ErrorUtils.throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        log.trace("Retrieved registered client");

        boolean grantAllowed = registeredClient.getAuthorizationGrantTypes().stream()
                .anyMatch(registeredGrantType -> registeredGrantType.getValue().equalsIgnoreCase(grantType.getCode()));
        if (!grantAllowed) {
            ErrorUtils.throwError(OAuth2ErrorCodes.INVALID_GRANT, OAuth2ParameterNames.CLIENT_ID);
        }

        if (StringUtils.isBlank(authenticationToken.getCredentials())) {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_PASSWORD, OAuth2ParameterNames.PASSWORD);
        }

        authenticationToken.setRegisteredClient(registeredClient);
        authenticationToken = authenticateToken(authenticationToken);

        Set<String> scopes = authenticationToken.getScopes();
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authenticationToken)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                .authorizedScopes(scopes)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantTypeMapper.INSTANCE.toSpring(grantType))
                .authorizationGrant(authenticationToken)
                .build();

        OAuth2Token generatedAccessToken = tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            ErrorUtils.throwError(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the access token.");
        }
        log.trace("Generated access token");

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(),
                generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(),
                tokenContext.getAuthorizedScopes()
        );

        OAuth2RefreshToken refreshToken = null;
        if (registeredClient.getAuthorizationGrantTypes().contains(org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN)
                && !registeredClient.getClientSettings().isRequireProofKey()) {
            OAuth2TokenContext refreshTokenContext = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(authenticationToken)
                    .authorizationServerContext(AuthorizationServerContextHolder.getContext())
                    .authorizedScopes(scopes)
                    .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                    .authorizationGrantType(AuthorizationGrantTypeMapper.INSTANCE.toSpring(grantType))
                    .authorizationGrant(authenticationToken)
                    .build();
            OAuth2Token generatedRefreshToken = tokenGenerator.generate(refreshTokenContext);
            if (generatedRefreshToken instanceof OAuth2RefreshToken oauth2RefreshToken) {
                refreshToken = oauth2RefreshToken;
            }
        }
        log.trace("Authenticated token request");
        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient,
                authenticationToken.getClientPrincipal(),
                accessToken,
                refreshToken
        );
    }

    protected abstract AuthorizationGrantType grantType();

    protected abstract T authenticateToken(T authenticationToken);
}
