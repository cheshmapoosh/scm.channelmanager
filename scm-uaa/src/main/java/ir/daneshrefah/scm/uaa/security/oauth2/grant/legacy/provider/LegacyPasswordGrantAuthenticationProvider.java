package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.provider;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.security.authentication.UaaPasswordAuthenticationFlowService;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyPasswordGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.oauth2.error.LegacyOAuth2ErrorMapper;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.RegisteredClientLegacyPolicy;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

/**
 * Legacy password provider kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@RequiredArgsConstructor
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPasswordGrantAuthenticationProvider implements AuthenticationProvider {
    private final UaaPasswordAuthenticationFlowService authenticationService;
    private final AuthenticationResponseTokenGenerator responseTokenGenerator;
    private final LegacyOAuth2ErrorMapper errorMapper;
    private final RegisteredClientLegacyPolicy legacyPolicy;
    private final RegisteredClientRepository registeredClientRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        Authentication clientPrincipal = preAuthenticationToken.getClientPrincipal();
        if (clientPrincipal instanceof OAuth2ClientAuthenticationToken clientAuthenticationToken) {
            preAuthenticationToken.setRegisteredClient(clientAuthenticationToken.getRegisteredClient());
        }
        if (preAuthenticationToken.getRegisteredClient() == null) {
            RegisteredClient registeredClient = registeredClientRepository.findByClientId(preAuthenticationToken.getClientId());
            preAuthenticationToken.setRegisteredClient(registeredClient);
        }
        try {
            if (preAuthenticationToken instanceof LegacyPasswordGrantAuthenticationToken legacyToken
                    && !legacyPolicy.isLegacyPasswordGrantAllowed(
                    preAuthenticationToken.getRegisteredClient(),
                    legacyToken.legacyClientType())) {
                ErrorUtils.throwError(OAuth2ErrorCodes.INVALID_GRANT, OAuth2ParameterNames.GRANT_TYPE);
            }
            UaaPasswordAuthenticationFlowService.AuthenticationResult result =
                    authenticationService.authenticate(preAuthenticationToken);
            return buildResponse(authentication, result.preAuthenticationToken(), result.authentication());
        } catch (Exception exception) {
            LegacyOAuth2ErrorMapper.LegacyError error = errorMapper.map(exception);
            ErrorUtils.throwError(error.errorCode(), error.parameterName());
            return null;
        }
    }

    private Authentication buildResponse(
            Authentication requestAuthentication,
            PreAuthenticationToken preAuthenticationToken,
            GeneralAuthenticationToken authentication
    ) {
        PostAuthenticationToken.AuthenticationStatus status = ((PostAuthenticationToken) authentication).getAuthenticationStatus();
        if (PostAuthenticationToken.AuthenticationStatus.INCOMPLETE.equals(status)) {
            throw new TwoStepAuthenticationRequiredException(authentication);
        }
        return responseTokenGenerator.getAccessToken(
                requestAuthentication,
                preAuthenticationToken.getClientPrincipal(),
                preAuthenticationToken.getRegisteredClient(),
                authentication
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return LegacyPasswordGrantAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
