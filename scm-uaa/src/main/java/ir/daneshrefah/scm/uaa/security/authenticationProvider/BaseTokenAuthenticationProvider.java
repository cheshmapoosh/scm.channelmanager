package ir.daneshrefah.scm.uaa.security.authenticationProvider;

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

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-27
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseTokenAuthenticationProvider<T extends AbstractAuthenticationToken> implements AuthenticationProvider {

    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        T authenticationToken = (T) authentication;

        String clientId = authenticationToken.getClientId();
        RegisteredClient registeredClient = this.registeredClientRepository.findByClientId(clientId);
        if (registeredClient == null) {
            ErrorUtils.throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        if (this.log.isTraceEnabled()) {
            this.log.trace("Retrieved registered client");
        }

        boolean isGrantTypeFound = registeredClient.getAuthorizationGrantTypes().stream().anyMatch(grantType -> grantType.getValue().equalsIgnoreCase(AuthorizationGrantType.SMS_OTP.getCode()));
        if (!isGrantTypeFound) {
            ErrorUtils.throwError(OAuth2ErrorCodes.INVALID_GRANT, OAuth2ParameterNames.CLIENT_ID);
        }

        if (StringUtils.isBlank(authenticationToken.getCredentials())) {
            ErrorUtils.throwError(Constants.OAUTH2_ERROR_CODE_INVALID_PASSWORD, OAuth2ParameterNames.PASSWORD);
        }

        authenticationToken.setRegisteredClient(registeredClient);
        authenticationToken = authenticateToken(authenticationToken);

        Set<String> scopes = authenticationToken.getScopes();
        // @formatter:off
        OAuth2TokenContext tokenContext = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authenticationToken)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
//                .authorization(authorization)
                .authorizedScopes(scopes)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .authorizationGrantType(AuthorizationGrantTypeMapper.GRANT_TYPE_SMS_OTP)
                .authorizationGrant(authenticationToken)
                .build();
        // @formatter:on

        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
        if (generatedAccessToken == null) {
            ErrorUtils.throwError(OAuth2ErrorCodes.SERVER_ERROR, "The token generator failed to generate the access token.");
        }

        if (this.log.isTraceEnabled()) {
            this.log.trace("Generated access token");
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());

        // @formatter:off
//        OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
//                .principalName(smsAuthentication.getPrincipal())
//                .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS)
//                .authorizedScopes(authorizedScopes);
//        // @formatter:on
//        if (generatedAccessToken instanceof ClaimAccessor) {
//            authorizationBuilder.token(accessToken, (metadata) ->
//                    metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
//        } else {
//            authorizationBuilder.accessToken(accessToken);
//        }
//
//        OAuth2Authorization authorization = authorizationBuilder.build();
//
//        this.authorizationService.save(authorization);

        if (this.log.isTraceEnabled()) {
            this.log.trace("Saved authorization");
            // This log is kept separate for consistency with other providers
            this.log.trace("Authenticated token request");
        }

        return new OAuth2AccessTokenAuthenticationToken(registeredClient, authenticationToken.getClientPrincipal(), accessToken);
    }

    protected abstract T authenticateToken(T authenticationToken);

}
