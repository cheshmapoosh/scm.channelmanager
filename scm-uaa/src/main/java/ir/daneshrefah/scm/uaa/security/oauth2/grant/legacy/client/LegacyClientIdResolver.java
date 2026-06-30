package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter.LegacyRequestParameters;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Legacy client-id resolution kept only for old NIB/PWA/MB/SA compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Component
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyClientIdResolver {
    private final LegacyAuthProperties properties;

    public LegacyClientIdResolver(LegacyAuthProperties properties) {
        this.properties = properties;
    }

    public String resolve(
            Authentication clientPrincipal,
            LegacyRequestParameters parameters,
            LegacyAppVersion appVersion,
            AuthorizationGrantType grantType
    ) {
        LegacyAppVersion effectiveAppVersion = appVersion == null ? new LegacyAppVersion(null) : appVersion;
        validate(effectiveAppVersion);

        String authenticatedClientId = authenticatedClientId(clientPrincipal);
        if (StringUtils.hasText(authenticatedClientId)) {
            return authenticatedClientId;
        }

        String explicitClientId = parameters.firstParameter(OAuth2ParameterNames.CLIENT_ID)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .orElse(null);
        if (StringUtils.hasText(explicitClientId)) {
            return explicitClientId;
        }

        if (AuthorizationGrantType.DEFAULT.equals(grantType) && effectiveAppVersion.isKnown()) {
            return requireClientId(configuredClientId(effectiveAppVersion.clientType()));
        }

        throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        return null;
    }

    private void validate(LegacyAppVersion appVersion) {
        if (appVersion.isPresent() && !appVersion.isKnown()) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_APP_VERSION, Constants.APP_VERSION_HEADER);
        }
    }

    private String authenticatedClientId(Authentication clientPrincipal) {
        if (!(clientPrincipal instanceof OAuth2ClientAuthenticationToken)
                || !clientPrincipal.isAuthenticated()
                || !StringUtils.hasText(clientPrincipal.getName())) {
            return null;
        }
        return clientPrincipal.getName().trim();
    }

    private String configuredClientId(LegacyClientType clientType) {
        LegacyAuthProperties.ClientResolution clientResolution = properties.getClientResolution();
        return switch (clientType) {
            case PWA -> clientResolution.getPwaClientId();
            case MB -> clientResolution.getMbClientId();
            case SA -> clientResolution.getSuperAppClientId();
            case NIB -> clientResolution.getNibClientId();
        };
    }

    private String requireClientId(String clientId) {
        if (!StringUtils.hasText(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        return clientId.trim();
    }
}
