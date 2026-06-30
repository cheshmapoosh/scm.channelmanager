package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyPasswordGrantRequest;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyAppVersion;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientIdResolver;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Legacy request mapper kept only for old NIB/PWA/MB/SA compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
@Component
public class LegacyPasswordGrantRequestMapper {
    private final LegacyClientIdResolver clientIdResolver;
    private final LegacyClientTypeResolver clientTypeResolver;

    public LegacyPasswordGrantRequestMapper(
            LegacyClientIdResolver clientIdResolver,
            LegacyClientTypeResolver clientTypeResolver
    ) {
        this.clientIdResolver = clientIdResolver;
        this.clientTypeResolver = clientTypeResolver;
    }

    public AuthorizationGrantType grantType(LegacyRequestParameters parameters) {
        return AuthorizationGrantType.findByCode(parameters.firstParameter(OAuth2ParameterNames.GRANT_TYPE).orElse(null));
    }

    public LegacyPasswordGrantRequest map(
            HttpServletRequest request,
            LegacyRequestParameters parameters,
            AuthorizationGrantType grantType
    ) {
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        String username = requiredSingle(parameters, OAuth2ParameterNames.USERNAME, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        String password = password(parameters, grantType);
        LegacyAppVersion appVersion = new LegacyAppVersion(parameters.appVersion().orElse(null));
        String clientId = clientIdResolver.resolve(clientPrincipal, parameters, appVersion, grantType);
        Set<String> scopes = scopes(parameters);
        LegacyClientType legacyClientType = clientTypeResolver.resolve(
                registeredClient(clientPrincipal),
                clientId,
                appVersion,
                grantType
        );
        return new LegacyPasswordGrantRequest(
                username,
                password,
                grantType,
                clientPrincipal,
                scopes,
                clientId,
                request.getRemoteAddr(),
                legacyClientType
        );
    }

    private String password(LegacyRequestParameters parameters, AuthorizationGrantType grantType) {
        Optional<String> value = parameters.firstParameter(OAuth2ParameterNames.PASSWORD);
        if (AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return decryptDefaultGrantPassword(value);
        }
        return value.filter(StringUtils::hasText)
                .orElseGet(() -> {
                    throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
                    return null;
                });
    }

    private String decryptDefaultGrantPassword(Optional<String> password) {
        return password.map(SecurityUtils.getInstance()::decryptPassword).orElse(null);
    }

    private RegisteredClient registeredClient(Authentication clientPrincipal) {
        if (clientPrincipal instanceof OAuth2ClientAuthenticationToken clientAuthenticationToken) {
            return clientAuthenticationToken.getRegisteredClient();
        }
        return null;
    }

    private String requiredSingle(LegacyRequestParameters parameters, String name, String errorParameter) {
        Optional<String> value = parameters.firstParameter(name);
        if (value.isEmpty() || !StringUtils.hasText(value.get())) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, errorParameter);
        }
        return value.get();
    }

    private Set<String> scopes(LegacyRequestParameters parameters) {
        return parameters
                .firstParameter(OAuth2ParameterNames.SCOPE)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .map(s -> new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(s, " "))))
                .orElse(null);
    }
}
