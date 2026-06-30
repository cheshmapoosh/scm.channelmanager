package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyClientType;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyPasswordGrantRequest;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.client.LegacyClientTypeResolver;
import ir.daneshrefah.scm.uaa.utils.RequestUtils;
import ir.daneshrefah.scm.uaa.utils.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Legacy request mapper kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyPasswordGrantRequestMapper {
    private final LegacyClientTypeResolver clientTypeResolver;

    public LegacyPasswordGrantRequestMapper(LegacyClientTypeResolver clientTypeResolver) {
        this.clientTypeResolver = clientTypeResolver;
    }

    public AuthorizationGrantType grantType(HttpServletRequest request) {
        return AuthorizationGrantType.findByCode(request.getParameter(OAuth2ParameterNames.GRANT_TYPE));
    }

    public LegacyPasswordGrantRequest map(
            HttpServletRequest request,
            ParameterSearch parameters,
            AuthorizationGrantType grantType
    ) {
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();
        String username = requiredSingle(parameters, OAuth2ParameterNames.USERNAME, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        String password = password(parameters, grantType);
        String appVersion = parameters.getFirst(Constants.APP_VERSION_HEADER).orElse(null);
        String clientId = clientId(clientPrincipal, request, appVersion, grantType);
        Set<String> scopes = scopes(parameters);
        LegacyClientType legacyClientType = clientTypeResolver.resolve(clientId, appVersion, grantType);
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

    private String password(ParameterSearch parameters, AuthorizationGrantType grantType) {
        Optional<String> value = parameters.getFirst(OAuth2ParameterNames.PASSWORD);
        if (AuthorizationGrantType.DEFAULT.equals(grantType)) {
            return value.map(SecurityUtils.getInstance()::decryptPassword).orElse(null);
        }
        return value.filter(StringUtils::hasText)
                .orElseGet(() -> {
                    throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
                    return null;
                });
    }

    private String clientId(
            Authentication clientPrincipal,
            HttpServletRequest request,
            String appVersion,
            AuthorizationGrantType grantType
    ) {
        String clientId;
        if (clientPrincipal == null || clientPrincipal instanceof AnonymousAuthenticationToken) {
            clientId = AuthorizationGrantType.DEFAULT.equals(grantType)
                    ? defaultClientId(appVersion)
                    : request.getParameter(OAuth2ParameterNames.CLIENT_ID);
        } else {
            clientId = clientPrincipal.getName();
        }
        if (!StringUtils.hasText(clientId)) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        return clientId;
    }

    private String defaultClientId(String appVersion) {
        if (StringUtils.startsWithIgnoreCase(appVersion, "MB")) {
            return "MB";
        }
        if (StringUtils.startsWithIgnoreCase(appVersion, "SA")) {
            return "SA";
        }
        return "PWA";
    }

    private String requiredSingle(ParameterSearch parameters, String name, String errorParameter) {
        Optional<String> value = parameters.getFirst(name);
        if (value.isEmpty() || !StringUtils.hasText(value.get())) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, errorParameter);
        }
        return value.get();
    }

    private Set<String> scopes(ParameterSearch parameters) {
        return parameters
                .getFirst(OAuth2ParameterNames.SCOPE)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .map(s -> new HashSet<>(Arrays.asList(StringUtils.delimitedListToStringArray(s, " "))))
                .orElse(null);
    }

    public static class ParameterSearch {
        private final MultiValueMap<String, String> parameters;

        public ParameterSearch(HttpServletRequest request, boolean includeHeaders) {
            parameters = getParameters(request, includeHeaders);
        }

        public Optional<String> getFirst(String parameterName) {
            return Optional.ofNullable(parameters.getFirst(org.apache.commons.lang3.StringUtils.toRootLowerCase(parameterName)));
        }

        private MultiValueMap<String, String> getParameters(HttpServletRequest request, boolean includeHeaders) {
            Map<String, String[]> parameterMap = request.getParameterMap();
            MultiValueMap<String, String> result = new LinkedMultiValueMap<>(parameterMap.size());
            if (includeHeaders) {
                result.putAll(RequestUtils.getRequestHeaders(request));
            }
            parameterMap.forEach((key, values) -> {
                for (String value : values) {
                    result.add(org.apache.commons.lang3.StringUtils.toRootLowerCase(key), value);
                }
            });
            return result;
        }
    }
}
